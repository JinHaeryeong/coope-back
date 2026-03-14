package com.coope.server.shared.security;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(@Nonnull Message<?> message, @Nonnull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            String authToken = accessor.getFirstNativeHeader("Authorization");
            if (authToken == null || !authToken.startsWith("Bearer ")) {
                log.warn("웹소켓 인증 실패 - Authorization 헤더 누락 또는 형식 오류");
                return null;
            }
            String token = authToken.substring(7);
            if (!jwtProvider.validateToken(token)) {
                log.warn("웹소켓 인증 실패 - 유효하지 않은 토큰");
                return null;
            }
            String userId = jwtProvider.getUserId(token);
            log.info("웹소켓 인증 성공 - 유저 PK: {}", userId);
            accessor.setUser(new StompPrincipal(userId));

        } else if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
            if (accessor.getUser() == null) {
                log.warn("웹소켓 권한 오류 - 인증되지 않은 세션의 {} 요청", command);
                return null;
            }
        }
        return message;
    }
}

class StompPrincipal implements Principal {
    private final String name;
    public StompPrincipal(String name) { this.name = name; }
    @Override public String getName() { return name; }
}
