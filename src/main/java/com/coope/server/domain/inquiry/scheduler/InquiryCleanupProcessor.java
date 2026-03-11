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
        List<String> urlsToDelete = inquiry.getFiles().stream()
                .map(InquiryFile::getFileUrl)
                .toList();

        inquiryRepository.hardDeleteById(inquiry.getId());

        deleteFilesSilently(inquiry.getId(), urlsToDelete);
    }

    private void deleteFilesSilently(Long inquiryId, List<String> urls) {
        for (String url : urls) {
            try {
                fileService.deleteFile(url, ImageCategory.INQUIRY);
            } catch (Exception e) {
                log.error("[Cleanup] S3 파일 삭제 도중 예외 발생 (inquiryId: {}, url: {})", inquiryId, url, e);
            }
        }
    }
}