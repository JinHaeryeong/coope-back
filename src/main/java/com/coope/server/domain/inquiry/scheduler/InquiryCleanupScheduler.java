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

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldInquiries() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);
        int totalDeleted = 0;

        while (true) {
            List<Inquiry> expiredInquiries = inquiryRepository.findAllExpiredInquiries(threshold);
            if (expiredInquiries.isEmpty()) break;

            for (Inquiry inquiry : expiredInquiries) {
                cleanupProcessor.processCleanup(inquiry);
                totalDeleted++;
            }
        }
        log.info("[Cleanup] 총 {}건 처리 완료.", totalDeleted);
    }
}