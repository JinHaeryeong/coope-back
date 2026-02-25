package com.coope.server.global.usage;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AiUsageService {
    private final RedisTemplate<String, Object> redisTemplate;

    public String getUsageKey(Long userId, String type) {
        return "ai:usage:" + LocalDate.now() + ":" + type + ":" + userId;
    }

    public int getRemainingCount(Long userId, String type, int maxCount) {
        String key = getUsageKey(userId, type);
        Object val = redisTemplate.opsForValue().get(key);
        int used = (val == null) ? 0 : Integer.parseInt(String.valueOf(val));
        return Math.max(0, maxCount - used);
    }
}