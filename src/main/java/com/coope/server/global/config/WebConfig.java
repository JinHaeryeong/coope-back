package com.coope.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);      // 기본 스레드 10개
        executor.setMaxPoolSize(50);      // 최대 50개까지 확장
        executor.setQueueCapacity(100);   // 대기 큐 100개
        executor.setThreadNamePrefix("ai-async-");
        executor.initialize();

        configurer.setTaskExecutor(executor);
        configurer.setDefaultTimeout(180000); // 3분 (AI 답변 길어질 때 대비)
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