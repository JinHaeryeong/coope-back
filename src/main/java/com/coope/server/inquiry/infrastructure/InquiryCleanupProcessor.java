package com.coope.server.inquiry.infrastructure;

import com.coope.server.inquiry.domain.Inquiry;
import com.coope.server.inquiry.domain.InquiryFile;
import com.coope.server.inquiry.domain.InquiryRepository;
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
        deleteFilesSilently(inquiry.getId(), urlsToDelete);
        inquiryRepository.hardDeleteById(inquiry.getId());
    }

    private void deleteFilesSilently(Long inquiryId, List<String> urls) {
        for (String url : urls) {
            try {
                boolean isDeleted = fileService.deleteFile(url, ImageCategory.INQUIRY);
                if (!isDeleted) {
                    log.warn("[Cleanup] S3 파일 삭제 실패 (inquiryId: {}, url: {})", inquiryId, url);
                }
            } catch (Exception e) {
                log.error("[Cleanup] S3 파일 삭제 도중 예외 발생 (inquiryId: {}, url: {})", inquiryId, url, e);
            }
        }
    }
}
