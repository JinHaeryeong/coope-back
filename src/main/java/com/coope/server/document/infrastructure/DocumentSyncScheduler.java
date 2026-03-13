package com.coope.server.document.infrastructure;

import com.coope.server.document.domain.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DocumentRepository documentRepository;

    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void syncRedisToDb() {
        Long size = redisTemplate.opsForSet().size("modified-documents");

        if (size == null || size == 0) {
            return;
        }
        log.info("[Scheduler] Redis -> DB 동기화 시작");
        int totalProcessed = 0;

        for (int i = 0; i < 100; i++) {
            Object idObj = redisTemplate.opsForSet().pop("modified-documents");

            if (idObj == null) {
                if (totalProcessed > 0) log.info("[Scheduler] 동기화 종료 (총 {}건 처리)", totalProcessed);
                break;
            }

            String documentIdStr = (String) idObj;
            String key = "document-snapshot:" + documentIdStr;

            try {
                Long documentId = Long.parseLong(documentIdStr);
                String content = (String) redisTemplate.opsForValue().get(key);

                if (content == null) {
                    log.warn("[Scheduler] 문서 ID {} - 스냅샷 유실(null), 명단에서만 제거", documentId);
                    continue;
                }
                if (content.length() < 180 || content.contains("\"content\":[]")) {
                    log.debug("[Scheduler] 문서 ID {} - 내용 부족으로 DB 동기화 건너뜀", documentId);
                    continue;
                }

                documentRepository.updateOnlyContent(documentId, content);
                redisTemplate.delete(key);
                totalProcessed++;
                log.info("[Scheduler] 문서 ID {} DB 동기화 완료", documentId);

            } catch (Exception e) {
                redisTemplate.opsForSet().add("modified-documents", documentIdStr);
                log.error("[Scheduler] 문서 ID {} 동기화 실패 (명단 복구): {}", documentIdStr, e.getMessage());
            }
        }
    }
}
