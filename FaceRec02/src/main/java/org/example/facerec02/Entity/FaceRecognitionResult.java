package org.example.facerec02.Entity;

public class FaceRecognitionResult {
    private String prediction;  // 用于存储真假预测的值
    private Double confidence;  // 用于存储预测的置信度

    // 构造器
    public FaceRecognitionResult(String prediction, Double confidence) {
        this.prediction = prediction;
        this.confidence = confidence;
    }

    // 获取预测结果
    public String getPrediction() {
        return prediction;
    }

    // 获取置信度
    public Double getConfidence() {
        return confidence;
    }

    // 判断是否为“真实”人脸
    public boolean isGenuine() {
        return "real".equalsIgnoreCase(prediction);
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

}
