package com.coope.server.notice.infrastructure;

import com.coope.server.notice.domain.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NoticeRepository noticeRepository;

    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void syncViewCountToDb() {
        ScanOptions options = ScanOptions.scanOptions()
                .match("notice:views:*")
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            int processedCount = 0;

            while (cursor.hasNext()) {
                String key = cursor.next();
                processSingleViewCountSync(key);
                processedCount++;
            }

            if (processedCount > 0) {
                log.info("[ViewCountScheduler] 동기화 완료: 총 {}개 반영", processedCount);
            }
        } catch (Exception e) {
            log.error("[ViewCountScheduler] SCAN 중 오류 발생: {}", e.getMessage());
        }
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
