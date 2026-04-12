package com.coope.server.auth.application;

import com.coope.server.auth.application.dto.LoginRequest;
import com.coope.server.auth.application.dto.LoginResponse;
import com.coope.server.user.domain.User;
import com.coope.server.user.application.UserService;
import com.coope.server.shared.config.JwtProperties;
import com.coope.server.shared.error.exception.AccountLockedException;
import com.coope.server.shared.error.exception.AuthenticationException;
import com.coope.server.shared.error.exception.InvalidTokenException;
import com.coope.server.shared.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final LoginAttemptService loginAttemptService;
    private final AccountRecoveryService accountRecoveryService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();

        if (loginAttemptService.isLocked(email)) {
            long remaining = loginAttemptService.getRemainingLockMinutes(email);
            throw new AccountLockedException(
                    "계정이 잠겼습니다. " + remaining + "분 후 재시도하거나 이메일 링크로 잠금을 해제해 주세요.");
        }

        User user;
        try {
            user = userService.validateUser(email, request.getPassword());
        } catch (Exception e) {
            boolean locked = loginAttemptService.recordFailure(email);

            if (locked) {
                accountRecoveryService.sendUnlockEmail(email);
                throw new AccountLockedException(
                        "로그인 5회 초과로 계정이 잠겼습니다. 가입 이메일로 잠금 해제 링크를 발송했습니다.");
            }

            int remaining = 5 - loginAttemptService.getFailCount(email);
            throw new AuthenticationException(
                    "이메일 또는 비밀번호가 일치하지 않습니다. (남은 시도: " + Math.max(remaining, 0) + "회)");
        }

        loginAttemptService.clearFailures(email);
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = createAndSaveRefreshToken(user.getEmail());
        return LoginResponse.of(user, accessToken, refreshToken);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        if (refreshToken != null) {
            try {
                redisTemplate.delete("RT:" + jwtProvider.getEmail(refreshToken));
                log.info("[Auth] RefreshToken 삭제 완료");
            } catch (Exception e) {
                log.warn("[Auth] 유효하지 않은 리프레시 토큰");
            }
        }
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            String token = accessToken.substring(7);
            try {
                long expiration = jwtProvider.getExpiration(token);
                if (expiration > 0) {
                    redisTemplate.opsForValue().set(token, "logout", expiration, TimeUnit.MILLISECONDS);
                    log.info("[Auth] AccessToken 블랙리스트 등록 완료");
                }
            } catch (Exception e) {
                log.warn("[Auth] 만료됐거나 잘못된 토큰");
            }
        }
    }

    @Transactional
    public String refresh(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }
        String email = jwtProvider.getEmail(refreshToken);
        String saved = (String) redisTemplate.opsForValue().get("RT:" + email);
        if (saved == null || !saved.equals(refreshToken)) {
            throw new InvalidTokenException("리프레시 토큰이 만료되었거나 일치하지 않습니다.");
        }
        User user = userService.findByEmail(email);
        return jwtProvider.createAccessToken(user.getId(), email, user.getRole().name());
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        String email = accountRecoveryService.verifyResetToken(resetToken);
        User user = userService.findByEmail(email);
        user.resetPassword(passwordEncoder.encode(newPassword));
        accountRecoveryService.consumeResetToken(resetToken);
        log.info("[Auth] 비밀번호 재설정 완료: {}", email);
    }

    private String createAndSaveRefreshToken(String email) {
        String token = jwtProvider.createRefreshToken(email);
        redisTemplate.opsForValue().set(
                "RT:" + email, token,
                jwtProperties.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);
        return token;
    }
}
