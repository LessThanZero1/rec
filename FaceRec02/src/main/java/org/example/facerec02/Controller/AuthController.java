package org.example.facerec02.Controller;

import cn.dev33.satoken.stp.StpUtil;
import org.example.facerec02.Entity.User;
import org.example.facerec02.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        long startTime = System.currentTimeMillis();
        try {
            logger.info("开始注册用户: {}", user.getUsername());
            User registeredUser = userService.registerUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("userId", registeredUser.getId());
            logger.info("用户注册成功，耗时: {}ms", System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("用户注册失败: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        long startTime = System.currentTimeMillis();
        String usernameOrEmail = credentials.get("username");
        String password = credentials.get("password");

        logger.info("开始登录用户: {}", usernameOrEmail);
        Optional<User> userOpt = userService.loginUser(usernameOrEmail, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            StpUtil.login(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", StpUtil.getTokenValue());
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            logger.info("用户登录成功，耗时: {}ms", System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(response);
        }

        logger.warn("用户登录失败: {}", usernameOrEmail);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "用户名/邮箱或密码错误");
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        StpUtil.logout();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "退出成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkLogin() {
        boolean isLogin = StpUtil.isLogin();
        Map<String, Object> response = new HashMap<>();
        response.put("isLogin", isLogin);
        if (isLogin) {
            response.put("userId", StpUtil.getLoginId());
        }
        return ResponseEntity.ok(response);
    }
} 