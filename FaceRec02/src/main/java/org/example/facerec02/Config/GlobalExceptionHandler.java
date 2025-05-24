package org.example.facerec02.Config;

import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ModelAndView handleNotLoginException(NotLoginException e) {
        // 重定向到登录页面
        return new ModelAndView("redirect:/login");
    }
} 