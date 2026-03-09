package com.coope.server.domain.notice.scheduler;

import com.coope.server.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component // 스캔 대상이 되어 자동으로 주기적 실행됨
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NoticeRepository noticeRepository;

    // 1분마다 실행 (테스트를 위해 짧게 설정, 나중에 10분 정도로 늘려도 됨)
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void syncViewCountToDb() {
        // "notice:views:"로 시작하는 모든 키를 가져옴
        Set<String> keys = redisTemplate.keys("notice:views:*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        log.info("조회수 동기화 시작: {} 개의 키 발견", keys.size());

        for (String key : keys) {
            try {
                // 키에서 ID 추출 (notice:views:5 -> 5)
                Long noticeId = Long.parseLong(key.split(":")[2]);

                // Redis에서 해당 공지사항의 누적 조회수 가져오기
                Object value = redisTemplate.opsForValue().get(key);

                if (value != null) {
                    int views = Integer.parseInt(value.toString());

                    // DB에 누적된 조회수만큼 더해주는 벌크 연산 수행
                    int updatedCount = noticeRepository.updateViews(noticeId, views);
                    log.info("공지사항 ID {}의 조회수 {}건이 DB에 반영되었습니다. (Rows: {})", noticeId, views, updatedCount);

                    // DB 반영이 성공하면 Redis에서 해당 키 삭제 (중복 방지)
                    redisTemplate.delete(key);
                }
            } catch (Exception e) {
                log.error("조회수 동기화 중 오류 발생 (Key: {}): {}", key, e.getMessage());
            }
        }
        log.info("조회수 동기화 완료");
    }
}