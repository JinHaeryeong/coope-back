package com.coope.server.global.usage;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AiUsageService {
    private final RedisTemplate<String, String> redisTemplate;

    public String getUsageKey(Long userId, String type) {
        return "ai:usage:" + LocalDate.now() + ":" + type + ":" + userId;
    }

    public int getRemainingCount(Long userId, String type, int maxCount) {
        String key = getUsageKey(userId, type);
        String val = redisTemplate.opsForValue().get(key);
        int used = (val == null) ? 0 : Integer.parseInt(val);
        return Math.max(0, maxCount - used);
    }
}