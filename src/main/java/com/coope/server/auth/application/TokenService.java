package com.coope.server.auth.application;

import com.coope.server.shared.error.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String UNLOCK_TOKEN_PREFIX = "UNLOCK_TOKEN:";
    private static final String RESET_TOKEN_PREFIX  = "RESET_TOKEN:";
    private static final Duration UNLOCK_TTL = Duration.ofMinutes(30);
    private static final Duration RESET_TTL  = Duration.ofMinutes(30);

    public String createUnlockToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(UNLOCK_TOKEN_PREFIX + token, email, UNLOCK_TTL);
        return token;
    }

    /** 토큰 검증 + 삭제(일회용). 유효하지 않으면 예외. */
    public String consumeUnlockToken(String token) {
        String email = redisTemplate.opsForValue().get(UNLOCK_TOKEN_PREFIX + token);
        if (email == null) throw new InvalidTokenException("잠금 해제 링크가 만료되었거나 유효하지 않습니다.");
        redisTemplate.delete(UNLOCK_TOKEN_PREFIX + token);
        return email;
    }

    public String createResetToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RESET_TOKEN_PREFIX + token, email, RESET_TTL);
        return token;
    }

    public String verifyResetToken(String token) {
        String email = redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + token);
        if (email == null) throw new InvalidTokenException("비밀번호 재설정 링크가 만료되었거나 유효하지 않습니다.");
        return email;
    }

    public void consumeResetToken(String token) {
        redisTemplate.delete(RESET_TOKEN_PREFIX + token);
    }
}
