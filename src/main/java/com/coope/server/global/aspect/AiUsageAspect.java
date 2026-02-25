package com.coope.server.global.aspect;

import com.coope.server.global.annotation.AiLimit;
import com.coope.server.global.error.exception.BadRequestException;
import com.coope.server.global.usage.AiUsageService;
import com.coope.server.global.util.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
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
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AiUsageAspect {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecurityUtil securityUtil;
    private final AiUsageService aiUsageService;

    @Around("@annotation(aiLimit)")
    public Object checkUsage(ProceedingJoinPoint joinPoint, AiLimit aiLimit) throws Throwable {
        Long userId = securityUtil.getCurrentUserId();

        int remainingBefore = aiUsageService.getRemainingCount(userId, aiLimit.type(), aiLimit.maxCount());

        if (remainingBefore <= 0) {
            throw new BadRequestException("오늘의 AI 사용 횟수(" + aiLimit.maxCount() + "회)를 모두 소모하셨습니다.");
        }

        Object result = joinPoint.proceed();

        String key = aiUsageService.getUsageKey(userId, aiLimit.type());
        Long newCountLong = redisTemplate.opsForValue().increment(key);
        int newCount = (newCountLong != null) ? newCountLong.intValue() : 0;
        int remainingAfter = aiLimit.maxCount() - newCount;

        if (newCount == 1) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            long secondsUntilMidnight = Duration.between(now, endOfToday).getSeconds();
            redisTemplate.expire(key, secondsUntilMidnight, TimeUnit.SECONDS);
            log.info("유저 {}의 {} AI 제한 설정 - 남은 시간: {}초", userId, aiLimit.type(), secondsUntilMidnight);
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletResponse response = attributes.getResponse();
            if (response != null) {
                response.setHeader("X-AI-Remaining", String.valueOf(remainingAfter));
                response.setHeader("Access-Control-Expose-Headers", "X-AI-Remaining");
            }
        }

        return result;
    }
}