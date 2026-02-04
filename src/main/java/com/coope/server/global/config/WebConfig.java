package com.coope.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 API 경로에 대해
                .allowedOrigins("http://localhost:5173") // 프론트엔드 포트 (Vite 기본값 기준)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // accessUrl이 "/images/"라면 "/images/**"로 매핑
        String pattern = accessUrl.endsWith("/") ? accessUrl + "**" : accessUrl + "/**";

        // uploadDir 경로를 file URL 형식으로 변환
        String location = "file:///" + uploadDir + (uploadDir.endsWith("/") ? "" : "/");

        registry.addResourceHandler(pattern)
                .addResourceLocations(location);
    }
}