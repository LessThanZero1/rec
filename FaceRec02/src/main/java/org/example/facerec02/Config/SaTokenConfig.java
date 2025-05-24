package org.example.facerec02.Config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    // 注册 Sa-Token 拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，定义拦截规则
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定拦截路由
            SaRouter.match("/**")
                    // 排除不需要登录的接口
                    .notMatch("/login", "/register", "/error", "/change-password")
                    .notMatch("/api/auth/login", "/api/auth/register", "/api/auth/check")
                    .notMatch("/css/**", "/js/**", "/images/**")
                    .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }
} 