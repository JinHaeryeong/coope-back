package com.coope.server.auth.application;

import com.coope.server.auth.application.dto.LoginRequest;
import com.coope.server.auth.application.dto.LoginResponse;
import com.coope.server.shared.config.JwtProperties;
import com.coope.server.shared.error.exception.AccountLockedException;
import com.coope.server.shared.error.exception.AuthenticationException;
import com.coope.server.shared.security.JwtProvider;
import com.coope.server.user.application.UserService;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.enums.Provider;
import com.coope.server.user.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    @Mock UserService userService;
    @Mock JwtProvider jwtProvider;
    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock JwtProperties jwtProperties;
    @Mock LoginAttemptService loginAttemptService;
    @Mock AccountRecoveryService accountRecoveryService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ValueOperations<String, Object> valueOps;

    @InjectMocks AuthService authService;

    private static final String EMAIL    = "test@coope.com";
    private static final String PASSWORD = "password123!";

    @Nested
    @DisplayName("로그인 성공")
    class LoginSuccess {

        @Test
        @DisplayName("올바른 인증 정보 → LoginResponse 반환 + 실패 기록 초기화")
        void successfulLogin() {
            User user = buildUser();
            LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

            given(loginAttemptService.isLocked(EMAIL)).willReturn(false);
            given(userService.validateUser(EMAIL, PASSWORD)).willReturn(user);
            given(jwtProvider.createAccessToken(any(), eq(EMAIL), anyString())).willReturn("access-token");
            given(jwtProvider.createRefreshToken(EMAIL)).willReturn("refresh-token");
            given(redisTemplate.opsForValue()).willReturn(valueOps);
            given(jwtProperties.getRefreshTokenExpiration()).willReturn(1209600000L);

            LoginResponse response = authService.login(request);

            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getEmail()).isEqualTo(EMAIL);
            verify(loginAttemptService).clearFailures(EMAIL);
        }
    }

    @Nested
    @DisplayName("계정 잠금 상태")
    class LockedAccount {

        @Test
        @DisplayName("이미 잠긴 계정 → AccountLockedException (남은 시간 포함)")
        void throwsWhenAlreadyLocked() {
            given(loginAttemptService.isLocked(EMAIL)).willReturn(true);
            given(loginAttemptService.getRemainingLockMinutes(EMAIL)).willReturn(25L);

            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
                    .isInstanceOf(AccountLockedException.class)
                    .hasMessageContaining("25분");
        }
    }

    @Nested
    @DisplayName("로그인 실패 및 잠금 처리")
    class LoginFailure {

        @Test
        @DisplayName("비밀번호 불일치 — 1~4회 → AuthenticationException + 남은 횟수 안내")
        void wrongPasswordShowsRemainingAttempts() {
            given(loginAttemptService.isLocked(EMAIL)).willReturn(false);
            given(userService.validateUser(EMAIL, PASSWORD))
                    .willThrow(new AuthenticationException("비밀번호가 일치하지 않습니다."));
            given(loginAttemptService.recordFailure(EMAIL)).willReturn(false);
            given(loginAttemptService.getFailCount(EMAIL)).willReturn(2);

            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("남은 시도: 3회");
        }

        @Test
        @DisplayName("5회 실패 → AccountLockedException + 잠금 해제 메일 발송")
        void fifthFailureLocksAndSendsMail() {
            given(loginAttemptService.isLocked(EMAIL)).willReturn(false);
            given(userService.validateUser(EMAIL, PASSWORD))
                    .willThrow(new AuthenticationException("비밀번호가 일치하지 않습니다."));
            given(loginAttemptService.recordFailure(EMAIL)).willReturn(true); // 5회 → 잠금

            assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, PASSWORD)))
                    .isInstanceOf(AccountLockedException.class)
                    .hasMessageContaining("5회 초과");

            verify(accountRecoveryService).sendUnlockEmail(EMAIL);
        }

        @Test
        @DisplayName("실패 후 성공하면 실패 기록 초기화됨")
        void clearsFailuresOnSuccess() {
            User user = buildUser();
            given(loginAttemptService.isLocked(EMAIL)).willReturn(false);
            given(userService.validateUser(EMAIL, PASSWORD)).willReturn(user);
            given(jwtProvider.createAccessToken(any(), eq(EMAIL), anyString())).willReturn("access-token");
            given(jwtProvider.createRefreshToken(EMAIL)).willReturn("refresh-token");
            given(redisTemplate.opsForValue()).willReturn(valueOps);
            given(jwtProperties.getRefreshTokenExpiration()).willReturn(1209600000L);

            authService.login(new LoginRequest(EMAIL, PASSWORD));

            verify(loginAttemptService).clearFailures(EMAIL);
            verify(loginAttemptService, never()).recordFailure(any());
        }
    }

    private User buildUser() {
        return User.builder()
                .email(EMAIL)
                .name("테스터")
                .nickname("tester")
                .provider(Provider.LOCAL)
                .role(Role.ROLE_USER)
                .build();
    }
}
