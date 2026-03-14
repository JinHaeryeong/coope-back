package com.coope.server.shared.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(() -> {
                    var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
                    factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
                    return factory;
                })
                .build();
    }
}