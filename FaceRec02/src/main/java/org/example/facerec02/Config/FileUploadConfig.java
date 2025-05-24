package org.example.facerec02.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    // 上传文件保存的根目录
    public static final String UPLOAD_DIR = "upload";
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将/upload/** 映射到项目根目录下的upload文件夹
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + Paths.get(UPLOAD_DIR).toAbsolutePath().toString() + "/");
    }
} 