package com.coope.server.domain.inquiry.scheduler;

import com.coope.server.domain.inquiry.entity.Inquiry;
import com.coope.server.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InquiryCleanupScheduler {

    private final InquiryRepository inquiryRepository;
    private final InquiryCleanupProcessor cleanupProcessor;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void cleanupOldInquiries() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);
        int totalDeleted = 0;
        int maxRetries = 100;

        log.info("[Cleanup] 스케줄러 시작 - 기준 시간: {}", threshold);

        for (int i = 0; i < maxRetries; i++) {
            List<Inquiry> expiredInquiries = inquiryRepository.findExpiredInquiriesWithLimit(threshold, 100);

            if (expiredInquiries.isEmpty()) break;

            for (Inquiry inquiry : expiredInquiries) {
                try {
                    cleanupProcessor.processCleanup(inquiry);
                    totalDeleted++;
                } catch (Exception e) {
                    log.error("[Cleanup] 개별 건 처리 실패 (inquiryId: {})", inquiry.getId(), e);
                }
            }
        }
        log.info("[Cleanup] 총 {}건 처리 완료.", totalDeleted);
    }
}