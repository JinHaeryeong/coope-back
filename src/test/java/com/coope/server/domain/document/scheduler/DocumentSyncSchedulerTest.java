package com.coope.server.domain.document.scheduler;

import com.coope.server.domain.document.entity.Document;
import com.coope.server.domain.document.repository.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DocumentSyncSchedulerTest {

    @Autowired
    private DocumentSyncScheduler scheduler;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private DocumentRepository documentRepository;

    @Test
    @DisplayName("Redis에 수정된 문서 ID가 있으면 DB 동기화 후 목록에서 제거되어야 한다")
    void syncRedisToDb_Success_Test() {
        Long testDocId = 28L;

        String longContent = "[{\"insert\":\"테스트 내용입니다. " + "가나다라".repeat(50) + "\"}]";
        String redisKey = "document-snapshot:" + testDocId;

        redisTemplate.opsForValue().set(redisKey, longContent);
        redisTemplate.opsForSet().add("modified-documents", testDocId.toString());

        scheduler.syncRedisToDb();

        Document updatedDoc = documentRepository.findById(testDocId).orElseThrow();
        assertThat(updatedDoc.getContent()).isEqualTo(longContent);

        Boolean isStillModified = redisTemplate.opsForSet().isMember("modified-documents", testDocId.toString());
        assertThat(isStillModified).isFalse();
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete("document-snapshot:28");
        redisTemplate.opsForSet().remove("modified-documents", "28");
    }
}
