package com.coope.server.domain.document.scheduler;

import com.coope.server.document.domain.Document;
import com.coope.server.document.domain.DocumentRepository;
import com.coope.server.document.infrastructure.DocumentSyncScheduler;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.UserRepository;
import com.coope.server.workspace.domain.Workspace;
import com.coope.server.workspace.domain.WorkspaceRepository;
import com.coope.server.support.AbstractContainerTest;
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
class DocumentSyncSchedulerTest extends AbstractContainerTest {

    @Autowired private DocumentSyncScheduler scheduler;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WorkspaceRepository workspaceRepository;

    private Long currentTestDocId;

    @Test
    @DisplayName("Redis에 수정된 문서 ID가 있으면 DB 동기화 후 목록에서 제거되어야 한다")
    void syncRedisToDb_Success_Test() {

        User testUser = userRepository.save(User.builder()
                .name("테스트")
                .nickname("테스트닉")
                .email("test@test.com")
                .build());
        Workspace testWorkspace = workspaceRepository.save(Workspace.builder()
                .name("테스트 워크스페이스")
                .creator(testUser)
                .inviteCode("ABCDE123")
                .build());
        Document doc = documentRepository.save(Document.builder()
                .title("테스트문서")
                .content("")
                .user(testUser)
                .workspace(testWorkspace)
                .build());
        currentTestDocId = doc.getId();

        String longContent = "[{\"insert\":\"테스트 내용입니다. " + "가나다라".repeat(50) + "\"}]";
        String redisKey = "document-snapshot:" + currentTestDocId;

        redisTemplate.opsForValue().set(redisKey, longContent);
        redisTemplate.opsForSet().add("modified-documents", currentTestDocId.toString());

        scheduler.syncRedisToDb();

        Document updatedDoc = documentRepository.findById(currentTestDocId).orElseThrow();
        assertThat(updatedDoc.getContent()).isEqualTo(longContent);

        Boolean isStillModified = redisTemplate.opsForSet().isMember("modified-documents", currentTestDocId.toString());
        assertThat(isStillModified).isFalse();
    }

    @AfterEach
    void tearDown() {
        if (currentTestDocId != null) {
            redisTemplate.delete("document-snapshot:" + currentTestDocId);
            redisTemplate.opsForSet().remove("modified-documents", currentTestDocId.toString());

            currentTestDocId = null;
        }
    }
}
