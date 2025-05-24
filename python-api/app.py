# app.py
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
import torch
from torchvision import models, transforms
import torch.nn as nn
from PIL import Image
import io
import logging
import numpy as np
import cv2

# -------------------------- 配置日志 --------------------------
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# -------------------------- 初始化FastAPI --------------------------
app = FastAPI()

# -------------------------- CORS配置 --------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["POST"],
    allow_headers=["*"],
)

# -------------------------- 设备配置 --------------------------
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
logger.info(f"Using device: {device}")

# -------------------------- 预处理配置 --------------------------
IMG_SIZE = 256  # 必须与训练时一致
transform = transforms.Compose([
    transforms.Resize((IMG_SIZE, IMG_SIZE)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],  # ImageNet标准参数
                         std=[0.229, 0.224, 0.225])
])

# -------------------------- 类别标签 --------------------------
CLASS_NAMES = ["fake", "real"]  # 必须与训练时的class_names顺序一致

# -------------------------- 模型加载 --------------------------
MODEL_PATH = "best_model.pth"  # 模型文件路径

def load_model():
    """加载模型结构和参数"""
    try:
        # 1. 重建模型结构
        model = models.resnet50(pretrained=False)
        num_features = model.fc.in_features
        model.fc = nn.Sequential(
            nn.Linear(num_features, 128),
            nn.ReLU(),
            nn.Dropout(0.4),
            nn.Linear(128, 2)
        )
        model = model.to(device)
        
        # 2. 加载训练好的参数
        state_dict = torch.load(MODEL_PATH, map_location=device)
        model.load_state_dict(state_dict)
        
        # 3. 设置为评估模式
        model.eval()  
        logger.info("Model loaded successfully")
        return model
        
    except Exception as e:
        logger.error(f"Model loading failed: {str(e)}")
        raise HTTPException(status_code=500, detail="模型加载失败")

# 全局加载模型
model = load_model()

# -------------------------- 核心预测函数 --------------------------
def predict_image(image_bytes: bytes) -> dict:
    """处理单张图片预测"""
    try:
        # 1. 转换字节流为PIL图像
        pil_image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        
        # 2. 应用预处理
        tensor = transform(pil_image).unsqueeze(0).to(device)  # 添加batch维度并发送到设备
        
        # 3. 推理
        with torch.no_grad():
            outputs = model(tensor)
            probabilities = torch.softmax(outputs, dim=1)
            conf, preds = torch.max(probabilities, 1)
        
        # 4. 解析结果
        return {
            "prediction": CLASS_NAMES[preds.item()],
            "confidence": round(conf.item() * 100, 2),
            "class_index": int(preds.item())
        }
        
    except Exception as e:
        logger.error(f"Prediction error: {str(e)}")
        raise HTTPException(status_code=400, detail="图片处理失败")

# -------------------------- API端点 --------------------------
@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    """预测接口"""
    try:
        # 1. 校验文件类型
        if not file.content_type.startswith("image/"):
            raise HTTPException(status_code=400, detail="仅支持图片文件")
            
        # 2. 读取文件内容
        contents = await file.read()
        if len(contents) > 10 * 1024 * 1024:  # 10MB限制
            raise HTTPException(status_code=400, detail="文件过大")
        
        # 3. 执行预测
        result = predict_image(contents)
        return JSONResponse(content=result)
        
    except HTTPException as he:
        raise
    except Exception as e:
        logger.error(f"API error: {str(e)}")
        raise HTTPException(status_code=500, detail="服务器内部错误")

# -------------------------- 服务启动 --------------------------
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)