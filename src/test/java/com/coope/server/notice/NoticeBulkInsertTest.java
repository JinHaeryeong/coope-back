package com.coope.server.notice;

import com.coope.server.domain.notice.dto.NoticeWriteRequest;
import com.coope.server.domain.notice.service.NoticeService;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class NoticeBulkInsertTest {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    @Rollback(false) // 데이터를 남기기 위해 false 유지
    @Disabled("더미 데이터 생성이 필요할 때만 수동으로 실행하세요.")
    void insert1000Notices() {
        // 어드민 계정 하나를 가져오기
        User admin = userRepository.findByEmail("admin@admin.com")
                .orElseThrow(() -> new RuntimeException("어드민 계정을 먼저 만들어주세요!"));

        // 1000개 밀어넣기
        for (int i = 1; i <= 1000; i++) {
            NoticeWriteRequest request = new NoticeWriteRequest(
                    "성능 테스트 공지사항 " + i,
                    "이것은 페이징 처리가 없는 조회의 성능을 테스트하기 위한 데이터입니다. 데이터 번호: " + i
            );
            noticeService.createNotice(request, admin, null);
        }

        System.out.println("1000개 데이터 삽입 완료!");
    }
}