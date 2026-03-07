package com.coope.server.domain.document.scheduler;

import com.coope.server.domain.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DocumentRepository documentRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void syncRedisToDb() {
        Set<String> keys = redisTemplate.keys("document-snapshot:*");
        if (keys.isEmpty()) return;

        log.info("[Scheduler] Redis -> DB 동기화 시작 (대상: {}건)", keys.size());

        for (String key : keys) {
            try {
                Long documentId = Long.parseLong(key.split(":")[1]);
                String content = (String) redisTemplate.opsForValue().get(key);

                if (content != null) {
                    if (content.length() < 200 || content.contains("\"content\":[]")) {
                        log.debug("[Scheduler] 문서 ID {} 보호 중 (내용 부족)", documentId);
                        continue;
                    }

                    documentRepository.updateOnlyContent(documentId, content);
                }
            } catch (Exception e) {
                log.error("[Scheduler] 문서 ID {} 동기화 중 오류 발생: {}", key, e.getMessage());
            }
        }
        log.info("[Scheduler] Redis -> DB 동기화 완료");
    }
}