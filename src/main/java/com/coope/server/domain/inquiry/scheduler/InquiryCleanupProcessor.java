package com.coope.server.domain.inquiry.scheduler;

import com.coope.server.domain.inquiry.entity.Inquiry;
import com.coope.server.domain.inquiry.entity.InquiryFile;
import com.coope.server.domain.inquiry.repository.InquiryRepository;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InquiryCleanupProcessor {

    private final InquiryRepository inquiryRepository;
    private final FileService fileService;

    @Transactional
    public void processCleanup(Inquiry inquiry) {
        try {
            List<String> urlsToDelete = inquiry.getFiles().stream()
                    .map(InquiryFile::getFileUrl)
                    .toList();

            inquiryRepository.hardDeleteById(inquiry.getId());

            urlsToDelete.forEach(url -> {
                boolean isDeleted = fileService.deleteFile(url, ImageCategory.INQUIRY);
                if (!isDeleted) {
                    log.warn("[Cleanup] S3 파일 삭제 실패 (inquiryId: {}, url: {})", inquiry.getId(), url);
                }
            });
        } catch (Exception e) {
            log.error("[Cleanup] inquiryId {} 처리 중 장애 발생", inquiry.getId(), e);
            throw e;
        }
    }
}