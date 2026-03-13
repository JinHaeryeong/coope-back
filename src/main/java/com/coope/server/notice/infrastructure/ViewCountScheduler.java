package com.coope.server.notice.infrastructure;

import com.coope.server.notice.domain.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NoticeRepository noticeRepository;

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void syncViewCountToDb() {
        Set<String> keys = redisTemplate.keys("notice:views:*");

        if (CollectionUtils.isEmpty(keys)) return;

        log.info("조회수 동기화 시작: {} 개의 키 발견", keys.size());
        for (String key : keys) {
            processSingleViewCountSync(key);
        }
        log.info("조회수 동기화 완료");
    }

    private void processSingleViewCountSync(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) return;

            Long noticeId = Long.parseLong(key.split(":")[2]);
            int views = Integer.parseInt(value.toString());

            int updatedCount = noticeRepository.updateViews(noticeId, views);

            if (updatedCount > 0) {
                log.info("ID {} 조회수 {}건 DB 반영 완료", noticeId, views);
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.error("조회수 동기화 중 오류 (Key: {}): {}", key, e.getMessage());
        }
    }
}
