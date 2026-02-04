package com.coope.server.domain.auth.service;

import com.coope.server.domain.auth.dto.LoginRequest;
import com.coope.server.domain.auth.dto.LoginResponse;
import com.coope.server.domain.auth.entity.RefreshToken;
import com.coope.server.domain.auth.repository.RefreshTokenRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.service.UserService;
import com.coope.server.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 유저 검증은 UserService에게 맡김
        User user = userService.validateUser(request);

        // 토큰 생성
        String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getRole().name());
        String refreshTokenValue = jwtProvider.createRefreshToken(user.getEmail());

        saveOrUpdateRefreshToken(user, refreshTokenValue);

        return LoginResponse.of(user, accessToken, refreshTokenValue);
    }

    private void saveOrUpdateRefreshToken(User user, String tokenValue) {
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateToken(tokenValue, LocalDateTime.now().plusDays(7)),
                        () -> refreshTokenRepository.save(new RefreshToken(user, tokenValue, LocalDateTime.now().plusDays(7)))
                );
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.deleteByTokenValue(refreshTokenValue);
    }

    @Transactional
    public String refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
        }

        User user = refreshToken.getUser();
        return jwtProvider.createAccessToken(user.getEmail(), user.getRole().name());
    }
}