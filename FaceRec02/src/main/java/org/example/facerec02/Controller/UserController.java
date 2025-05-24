package org.example.facerec02.Controller;

import cn.dev33.satoken.stp.StpUtil;
import org.example.facerec02.Entity.User;
import org.example.facerec02.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // 用户登录页面
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // 用户登录处理
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        Optional<User> userOpt = userService.loginUser(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 使用 Sa-Token 登录
            StpUtil.login(user.getId());
            return "redirect:/recognize";
        }
        return "redirect:/login?error=invalid_credentials"; // 登录失败，添加错误类型
    }

    // 用户注册页面
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    // 用户注册处理
    @PostMapping("/register")
    public String register(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/login"; // 注册成功，跳转到登录页
    }

    // 用户密码修改页面
    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "change-password";
    }

    // 用户密码修改处理
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String username, @RequestParam String oldPassword, @RequestParam String newPassword) {
        // 验证用户名和旧密码
        Optional<User> userOpt = userService.loginUser(username, oldPassword);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (userService.changePassword(user.getId(), oldPassword, newPassword)) {
                return "redirect:/login?success=password_changed";
            }
        }
        return "redirect:/change-password?error"; // 密码修改失败
    }

    // 用户登出处理
    @GetMapping("/logout")
    public String logout() {
        StpUtil.logout();
        return "redirect:/login"; // 退出后跳转到登录页
    }
}
