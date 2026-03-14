package com.coope.server.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.SecureRandom;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    @Bean(name = "aiTaskExecutor")
    public ThreadPoolTaskExecutor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-async-");
        executor.initialize();
        return executor;
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(aiTaskExecutor());
        configurer.setDefaultTimeout(180000);
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