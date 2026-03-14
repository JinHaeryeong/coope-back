package com.coope.server.auth.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final long FAIL_WINDOW_MINUTES = 10;   // 실패 카운트 유지 시간
    private static final long LOCK_DURATION_MINUTES = 30; // 계정 잠금 시간

    private static final String FAIL_KEY_PREFIX   = "LOGIN_FAIL:";
    private static final String LOCKED_KEY_PREFIX = "LOGIN_LOCKED:";

    public boolean isLocked(String email) {
        return redisTemplate.hasKey(LOCKED_KEY_PREFIX + email);
    }

    public long getRemainingLockMinutes(String email) {
        Long seconds = redisTemplate.getExpire(LOCKED_KEY_PREFIX + email, TimeUnit.SECONDS);
        if (seconds == null || seconds <= 0) return 0;
        return (seconds + 59) / 60;
    }
    
    // 로그인 실패 처리
    public boolean recordFailure(String email) {
        String failKey = FAIL_KEY_PREFIX + email;

        Long count = redisTemplate.opsForValue().increment(failKey);

        // 첫 실패 시 TTL 설정
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, FAIL_WINDOW_MINUTES, TimeUnit.MINUTES);
        }

        if (count != null && count >= MAX_ATTEMPTS) {
            lock(email);
            redisTemplate.delete(failKey); // 카운트 초기화
            log.warn("[LoginAttempt] 계정 잠금 처리: {}", email);
            return true;
        }

        log.info("[LoginAttempt] 로그인 실패 {}/{}회: {}", count, MAX_ATTEMPTS, email);
        return false;
    }

    public void clearFailures(String email) {
        redisTemplate.delete(FAIL_KEY_PREFIX + email);
    }

    public void lock(String email) {
        redisTemplate.opsForValue().set(
                LOCKED_KEY_PREFIX + email,
                "locked",
                LOCK_DURATION_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public void unlock(String email) {
        redisTemplate.delete(LOCKED_KEY_PREFIX + email);
        redisTemplate.delete(FAIL_KEY_PREFIX + email);
        log.info("[LoginAttempt] 계정 잠금 해제: {}", email);
    }

    public int getFailCount(String email) {
        Object val = redisTemplate.opsForValue().get(FAIL_KEY_PREFIX + email);
        if (val == null) return 0;
        try { return Integer.parseInt(String.valueOf(val)); }
        catch (NumberFormatException e) { return 0; }
    }
}
