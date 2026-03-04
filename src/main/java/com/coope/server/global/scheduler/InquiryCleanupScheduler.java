package com.coope.server.global.scheduler;

import com.coope.server.domain.inquiry.entity.Inquiry;
import com.coope.server.domain.inquiry.entity.InquiryFile;
import com.coope.server.domain.inquiry.repository.InquiryRepository;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InquiryCleanupScheduler {

    private final InquiryRepository inquiryRepository;
    private final FileService fileService;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void cleanupOldInquiries() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);

        List<Inquiry> expiredInquiries = inquiryRepository.findAllExpiredInquiries(threshold);

        for (Inquiry inquiry : expiredInquiries) {
            try {
                List<String> urlsToDelete = inquiry.getFiles().stream()
                        .map(InquiryFile::getFileUrl)
                        .toList();

                inquiryRepository.hardDeleteById(inquiry.getId());

                urlsToDelete.forEach(url ->
                        fileService.deleteFile(url, ImageCategory.INQUIRY)
                );
            } catch (Exception e) {
                log.error("inquiryId {} 청소 중 에러: {}", inquiry.getId(), e.getMessage());
            }
        }
    }
}