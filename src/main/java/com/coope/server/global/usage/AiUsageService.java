package com.coope.server.global.usage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiUsageService {
    private final RedisTemplate<String, Object> redisTemplate;

    public String getUsageKey(Long userId, String type) {
        return "ai:usage:" + LocalDate.now() + ":" + type + ":" + userId;
    }

    public int getRemainingCount(Long userId, String type, int maxCount) {
        String key = getUsageKey(userId, type);
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) return maxCount;

        try {
            int used = Integer.parseInt(String.valueOf(val));
            return Math.max(0, maxCount - used);
        } catch (NumberFormatException e) {
            log.error("Redis AI 사용량 데이터 파싱 실패 - key: {}, value: {}", key, val);
            return maxCount;
        }
    }
}