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
        Set<Object> modifiedIds = redisTemplate.opsForSet().members("modified-documents");

        if (modifiedIds == null || modifiedIds.isEmpty()) {
            return;
        }

        log.info("[Scheduler] Redis -> DB 동기화 시작 (수정 대상: {}건)", modifiedIds.size());

        for (Object idObj : modifiedIds) {
            String documentIdStr = (String) idObj;
            String key = "document-snapshot:" + documentIdStr;

            try {
                Long documentId = Long.parseLong(documentIdStr);
                String content = (String) redisTemplate.opsForValue().get(key);

                if (content != null) {
                    if (content.length() < 180 || content.contains("\"content\":[]")) {
                        redisTemplate.opsForSet().remove("modified-documents", documentIdStr);
                        log.info("[Scheduler] 문서 ID {} - 내용 부족으로 DB 저장 건너뜀 (명단 제거)", documentId);
                        continue;
                    }

                    documentRepository.updateOnlyContent(documentId, content);

                    redisTemplate.opsForSet().remove("modified-documents", documentIdStr);
                    log.info("[Scheduler] 문서 ID {} DB 동기화 완료", documentId);
                }
            } catch (Exception e) {
                log.error("[Scheduler] 문서 ID {} 동기화 중 오류: {}", documentIdStr, e.getMessage());
            }
        }
        log.info("[Scheduler] Redis -> DB 동기화 사이클 종료");
    }
}