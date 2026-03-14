package com.coope.server.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock RedisTemplate<String, Object> redisTemplate;
    @Mock ValueOperations<String, Object> valueOps;

    @InjectMocks LoginAttemptService loginAttemptService;

    private static final String EMAIL = "test@coope.com";
    private static final String FAIL_KEY   = "LOGIN_FAIL:" + EMAIL;
    private static final String LOCKED_KEY = "LOGIN_LOCKED:" + EMAIL;

    @Nested
    @DisplayName("isLocked()")
    class IsLocked {

        @Test
        @DisplayName("잠금 키가 존재하면 true 반환")
        void returnsTrueWhenLocked() {
            given(redisTemplate.hasKey(LOCKED_KEY)).willReturn(true);
            assertThat(loginAttemptService.isLocked(EMAIL)).isTrue();
        }

        @Test
        @DisplayName("잠금 키가 없으면 false 반환")
        void returnsFalseWhenNotLocked() {
            given(redisTemplate.hasKey(LOCKED_KEY)).willReturn(false);
            assertThat(loginAttemptService.isLocked(EMAIL)).isFalse();
        }
    }

    @Nested
    @DisplayName("recordFailure()")
    class RecordFailure {

        @BeforeEach
        void setUp() {
            given(redisTemplate.opsForValue()).willReturn(valueOps);
        }

        @Test
        @DisplayName("첫 번째 실패 — TTL 설정, 잠금 안 됨")
        void firstFailureSetsExpiry() {
            given(valueOps.increment(FAIL_KEY)).willReturn(1L);

            boolean locked = loginAttemptService.recordFailure(EMAIL);

            assertThat(locked).isFalse();
            verify(redisTemplate).expire(eq(FAIL_KEY), eq(10L), eq(TimeUnit.MINUTES));
        }

        @Test
        @DisplayName("4번째 실패 — 잠금 안 됨")
        void fourthFailureNotLocked() {
            given(valueOps.increment(FAIL_KEY)).willReturn(4L);

            boolean locked = loginAttemptService.recordFailure(EMAIL);

            assertThat(locked).isFalse();
        }

        @Test
        @DisplayName("5번째 실패 — 계정 잠금, true 반환")
        void fifthFailureLocksAccount() {
            given(valueOps.increment(FAIL_KEY)).willReturn(5L);

            boolean locked = loginAttemptService.recordFailure(EMAIL);

            assertThat(locked).isTrue();
            verify(valueOps).set(eq(LOCKED_KEY), eq("locked"), eq(30L), eq(TimeUnit.MINUTES));
            verify(redisTemplate).delete(FAIL_KEY);
        }

        @Test
        @DisplayName("6번째 실패도 잠금 처리 — true 반환")
        void sixthFailureStillLocked() {
            given(valueOps.increment(FAIL_KEY)).willReturn(6L);

            boolean locked = loginAttemptService.recordFailure(EMAIL);

            assertThat(locked).isTrue();
        }
    }

    @Nested
    @DisplayName("clearFailures()")
    class ClearFailures {

        @Test
        @DisplayName("로그인 성공 시 실패 키 삭제")
        void deletesFailKey() {
            loginAttemptService.clearFailures(EMAIL);
            verify(redisTemplate).delete(FAIL_KEY);
        }
    }

    @Nested
    @DisplayName("unlock()")
    class Unlock {

        @Test
        @DisplayName("잠금 키와 실패 키 모두 삭제")
        void deletesBothKeys() {
            loginAttemptService.unlock(EMAIL);
            verify(redisTemplate).delete(LOCKED_KEY);
            verify(redisTemplate).delete(FAIL_KEY);
        }
    }

    @Nested
    @DisplayName("getFailCount()")
    class GetFailCount {

        @BeforeEach
        void setUp() {
            given(redisTemplate.opsForValue()).willReturn(valueOps);
        }

        @Test
        @DisplayName("Redis 값이 없으면 0 반환")
        void returnsZeroWhenNoKey() {
            given(valueOps.get(FAIL_KEY)).willReturn(null);
            assertThat(loginAttemptService.getFailCount(EMAIL)).isZero();
        }

        @Test
        @DisplayName("Redis 값이 있으면 파싱해서 반환")
        void returnsParsedCount() {
            given(valueOps.get(FAIL_KEY)).willReturn("3");
            assertThat(loginAttemptService.getFailCount(EMAIL)).isEqualTo(3);
        }

        @Test
        @DisplayName("파싱 실패 시 0 반환")
        void returnsZeroOnParseError() {
            given(valueOps.get(FAIL_KEY)).willReturn("invalid");
            assertThat(loginAttemptService.getFailCount(EMAIL)).isZero();
        }
    }
}
