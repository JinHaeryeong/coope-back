package com.coope.server.domain.auth.service;

import com.coope.server.domain.auth.dto.LoginRequest;
import com.coope.server.domain.auth.dto.LoginResponse;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.service.UserService;
import com.coope.server.global.config.JwtProperties;
import com.coope.server.global.error.exception.InvalidTokenException;
import com.coope.server.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;


    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userService.validateUser(request);

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshTokenValue = createAndSaveRefreshToken(user.getEmail());

        return LoginResponse.of(user, accessToken, refreshTokenValue);
    }

    @Transactional
    public void logout(String accessToken, String refreshTokenValue) {
        handleRefreshTokenRemoval(refreshTokenValue);

        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            return;
        }

        String token = accessToken.substring(7);
        try {
            long expiration = jwtProvider.getExpiration(token);
            if (expiration > 0) {
                redisTemplate.opsForValue().set(token, "logout", expiration, TimeUnit.MILLISECONDS);
                log.info("[Auth] AccessToken 블랙리스트 등록 완료");
            }
        } catch (Exception e) {
            log.warn("[Auth] 이미 만료된 토큰이거나 잘못된 토큰입니다.");
        }
    }

    private void handleRefreshTokenRemoval(String refreshTokenValue) {
        if (refreshTokenValue == null) return;

        try {
            String email = jwtProvider.getEmail(refreshTokenValue);
            redisTemplate.delete(getRefreshTokenKey(email));
            log.info("[Auth] RefreshToken 삭제 완료: {}", email);
        } catch (Exception e) {
            log.warn("[Auth] 로그아웃 중 유효하지 않은 리프레시 토큰 감지");
        }
    }

    @Transactional
    public String refresh(String refreshTokenValue) {
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = jwtProvider.getEmail(refreshTokenValue);
        String savedToken = (String) redisTemplate.opsForValue().get(getRefreshTokenKey(email));

        if (savedToken == null || !savedToken.equals(refreshTokenValue)) {
            throw new InvalidTokenException("리프레시 토큰이 만료되었거나 일치하지 않습니다.");
        }

        User user = userService.findByEmail(email);

        return jwtProvider.createAccessToken(user.getId(), email, user.getRole().name());
    }

    private String createAndSaveRefreshToken(String email) {
        String refreshToken = jwtProvider.createRefreshToken(email);
        redisTemplate.opsForValue().set(
                getRefreshTokenKey(email),
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
        return refreshToken;
    }

    private String getRefreshTokenKey(String email) {
        return "RT:" + email;
    }
}