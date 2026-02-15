package com.coope.server.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import jakarta.annotation.Nonnull;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(@Nonnull Message<?> message, @Nonnull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authToken = accessor.getFirstNativeHeader("Authorization");
            log.info("웹소켓 연결 시도 - 헤더 확인: {}", authToken);

            if (authToken != null && authToken.startsWith("Bearer ")) {
                String token = authToken.substring(7);

                if (jwtProvider.validateToken(token)) {
                    String userId = jwtProvider.getUserId(token);
                    log.info("웹소켓 인증 성공 - 유저 PK: {}", userId);

                    accessor.setUser(new StompPrincipal(userId));
                } else {
                    log.warn("웹소켓 인증 실패 - 유효하지 않은 토큰");
                }
            }
        }
        return message;
    }
}

// 웹소켓 세션 동안 유지될 유저 명찰
class StompPrincipal implements Principal {
    private final String name;
    public StompPrincipal(String name) { this.name = name; }
    @Override
    public String getName() { return name; }
}