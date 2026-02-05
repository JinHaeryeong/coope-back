package com.coope.server.domain.auth.service;

import com.coope.server.domain.auth.dto.LoginRequest;
import com.coope.server.domain.auth.dto.LoginResponse;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.service.UserService;
import com.coope.server.global.config.JwtProperties;
import com.coope.server.global.error.exception.InvalidTokenException;
import com.coope.server.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProperties jwtProperties;


    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userService.validateUser(request);

        // 토큰 생성
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshTokenValue = jwtProvider.createRefreshToken(user.getEmail());

        // 레디스에 Refresh Token 저장 (Key: 이메일, Value: 토큰값)
        // 앞에 RT 같은 거 붙여주면 나중에 구분하기 좋음
        redisTemplate.opsForValue().set(
                "RT:" + user.getEmail(),
                refreshTokenValue,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return LoginResponse.of(user, accessToken, refreshTokenValue);
    }

    @Transactional
    public void logout(String accessToken, String refreshTokenValue) {
        // 레디스에서 삭제
        if (refreshTokenValue != null) {
            String email = jwtProvider.getEmail(refreshTokenValue);
            redisTemplate.delete("RT:" + email);
        }

        // 액세스 토큰 블랙리스트 등록
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String token = accessToken.substring(7); // "Bearer " 제거
            long expiration = jwtProvider.getExpiration(token);

            redisTemplate.opsForValue().set(token, "logout", expiration, TimeUnit.MILLISECONDS);
        }
    }

    @Transactional
    public String refresh(String refreshTokenValue) {
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = jwtProvider.getEmail(refreshTokenValue);
        String savedToken = (String) redisTemplate.opsForValue().get("RT:" + email);

        if (savedToken == null || !savedToken.equals(refreshTokenValue)) {
            throw new InvalidTokenException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
        }

        User user = userService.findByEmail(email);

        return jwtProvider.createAccessToken(email, user.getRole().name());
    }
}