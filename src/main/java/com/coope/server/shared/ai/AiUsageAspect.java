package com.coope.server.shared.ai;

import com.coope.server.shared.error.exception.BadRequestException;
import com.coope.server.shared.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AiUsageAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityUtil securityUtil;
    private final AiUsageService aiUsageService;

    @Around("@annotation(aiLimit)")
    public Object checkUsage(ProceedingJoinPoint joinPoint, AiLimit aiLimit) throws Throwable {
        Long userId = securityUtil.getCurrentUserId();
        String key = aiUsageService.getUsageKey(userId, aiLimit.type());

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount > aiLimit.maxCount()) {
            redisTemplate.opsForValue().decrement(key);
            throw new BadRequestException("오늘의 AI 사용 횟수(" + aiLimit.maxCount() + "회)를 모두 소모하셨습니다.");
        }

        if (currentCount != null && currentCount == 1) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
            long secondsUntilMidnight = Duration.between(now, startOfTomorrow).getSeconds();
            redisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
        }

        Object result = joinPoint.proceed();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getResponse() != null) {
            int remaining = aiLimit.maxCount() - (currentCount != null ? currentCount.intValue() : 0);
            attributes.getResponse().setHeader("X-AI-Remaining", String.valueOf(remaining));
            attributes.getResponse().setHeader("Access-Control-Expose-Headers", "X-AI-Remaining");
        }

        return result;
    }
}
