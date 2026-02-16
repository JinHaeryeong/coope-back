package com.coope.server.global.config;

import com.coope.server.global.security.FilterChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// config/WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final FilterChannelInterceptor filterChannelInterceptor;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지를 받을 때: /topic(공통), /queue(개인)
        config.enableSimpleBroker("/topic", "/queue");
        // 메시지를 보낼 때: /app/메시지경로
        config.setApplicationDestinationPrefixes("/app");
        // 특정 유저에게 보낼 때 사용할 접두사
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp") // 엔드포인트
                .setAllowedOriginPatterns(clientUrl);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(filterChannelInterceptor);
    }
}