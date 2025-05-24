package org.example.facerec02.Controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpSession;
import org.example.facerec02.Config.FileUploadConfig;
import org.example.facerec02.Entity.FaceRecognitionHistory;
import org.example.facerec02.Entity.User;
import org.example.facerec02.Service.FaceRecognitionService;
import org.example.facerec02.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class FaceRecognitionController {
    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @Autowired
    private UserService userService;

    @GetMapping("/recognize")
    public String showRecognitionForm() {
        return "recognize";
    }
    @GetMapping("/explanation")
    public String explanation() {
        return "explanation";
    }

    @PostMapping("/recognize")
    public String recognizeFace(@RequestParam("photo") MultipartFile photo, HttpSession session, Model model) throws IOException {
        // 确保上传目录存在
        File uploadDir = new File(FileUploadConfig.UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成带时间戳的文件名
        String originalFilename = photo.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String newFilename = timestamp + "_" + uniqueId + extension;

        // 保存文件
        Path filePath = Paths.get(FileUploadConfig.UPLOAD_DIR, newFilename);
        Files.copy(photo.getInputStream(), filePath);

        // 调用识别服务
        FaceRecognitionService.FaceRecognitionResult result = faceRecognitionService.recognizeFace(photo);

        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        Optional<User> userOpt = userService.getUserById(userId);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 保存识别历史
            FaceRecognitionHistory history = new FaceRecognitionHistory();
            history.setUser(user);  // 关联用户
            history.setImageUrl("/upload/" + newFilename);  // 保存文件路径
            history.setGenuine(result.isGenuine());  // 识别结果是否为真
            history.setConfidence(result.getConfidence());  // 识别的置信度
            history.setCreateTime(DateUtil.now());
            userService.saveHistory(history);  // 使用UserService保存记录
        }

        // 将结果添加到Model中
        model.addAttribute("result", result);
        model.addAttribute("imageUrl", "/upload/" + newFilename);
        
        return "result";  // 返回result页面
    }

    @GetMapping("/history")
    public String viewHistory(HttpSession session, Model model) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<FaceRecognitionHistory> history = userService.getHistory(userId);
        model.addAttribute("history", history);
        return "history";
    }

    @DeleteMapping("/api/history/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteHistory(@PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean success = userService.deleteHistory(id, userId);
            
            if (success) {
                return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "删除成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "删除失败：记录不存在或无权限"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "删除失败：" + e.getMessage()
            ));
        }
    }
}
