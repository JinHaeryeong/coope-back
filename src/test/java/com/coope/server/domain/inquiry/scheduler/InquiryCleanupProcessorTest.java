package com.coope.server.domain.inquiry.scheduler;

import com.coope.server.inquiry.domain.Inquiry;
import com.coope.server.inquiry.domain.InquiryFile;
import com.coope.server.inquiry.domain.InquiryRepository;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import com.coope.server.inquiry.infrastructure.InquiryCleanupProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryCleanupProcessorTest {

    @Mock private InquiryRepository inquiryRepository;
    @Mock private FileService fileService;

    @InjectMocks
    private InquiryCleanupProcessor cleanupProcessor;

    @Test
    @DisplayName("영구 삭제 시 S3 파일이 먼저 삭제된 후 DB 데이터가 삭제되어야 한다") // 제목도 맞춰주기!
    void processCleanup_SuccessOrder() {
        // given
        Long inquiryId = 1L;
        String fileUrl = "http://s3.com/test.jpg";

        Inquiry inquiry = Inquiry.builder().build();
        ReflectionTestUtils.setField(inquiry, "id", inquiryId);

        InquiryFile file = InquiryFile.builder().fileUrl(fileUrl).build();
        ReflectionTestUtils.setField(inquiry, "files", List.of(file));

        when(fileService.deleteFile(anyString(), eq(ImageCategory.INQUIRY))).thenReturn(true);

        // when
        cleanupProcessor.processCleanup(inquiry);

        // then
        InOrder inOrder = inOrder(inquiryRepository, fileService);

        inOrder.verify(fileService).deleteFile(fileUrl, ImageCategory.INQUIRY);
        inOrder.verify(inquiryRepository).hardDeleteById(inquiryId);
    }
}