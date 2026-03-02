package com.coope.server.domain.inquiry.service;

import com.coope.server.domain.inquiry.dto.InquiryCreateRequest;
import com.coope.server.domain.inquiry.entity.Inquiry;
import com.coope.server.domain.inquiry.enums.InquiryCategory;
import com.coope.server.domain.inquiry.repository.InquiryRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.enums.Role;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock private InquiryRepository inquiryRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileService fileService;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    @DisplayName("문의사항 등록 성공 - 이미지 포함")
    void createInquiry_success() {
        // given
        Long userId = 1L;

        User user = User.builder()
                .email("test@coope.com")
                .name("테스터")
                .nickname("테스터")
                .build();

        MockMultipartFile file = new MockMultipartFile("files", "test.jpg", "image/jpeg", "data".getBytes());
        InquiryCreateRequest request = InquiryCreateRequest.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .category(InquiryCategory.ERROR)
                .environment("Android")
                .files(List.of(new MockMultipartFile("files", "test.png", "image/png", "test".getBytes())))
                .build();
        request.setTitle("문의 제목");
        request.setContent("문의 내용");
        request.setFiles(List.of(file));

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(fileService.upload(any(), eq(ImageCategory.INQUIRY))).willReturn("http://s3-url.com/test.jpg");

        // save 시 들어온 객체를 그대로 반환 (id가 없어도 save 호출 여부만 확인하면 됨)
        given(inquiryRepository.save(any(Inquiry.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Long inquiryId = inquiryService.createInquiry(userId, request);

        // then
        verify(fileService, times(1)).upload(any(), eq(ImageCategory.INQUIRY));
        verify(inquiryRepository).save(any(Inquiry.class));
    }

    @Test
    @DisplayName("문의사항 삭제 성공 - 작성자 본인일 때")
    void deleteInquiry_success_by_owner() {
        // given
        Long inquiryId = 1L;
        Long userId = 100L;

        User user = User.builder().email("test@coope.com").name("테스터").build();
        ReflectionTestUtils.setField(user, "id", userId); // ID 강제 주입

        Inquiry inquiry = Inquiry.createInquiry(user, "제목", "내용", InquiryCategory.ERROR, "PC");
        inquiry.addFile("old-file.jpg", "test.jpg");

        given(inquiryRepository.findById(inquiryId)).willReturn(Optional.of(inquiry));

        // when
        inquiryService.deleteInquiry(inquiryId, userId, Role.ROLE_USER);

        // then
        verify(fileService).deleteFile(eq("old-file.jpg"), eq(ImageCategory.INQUIRY));
        verify(inquiryRepository).delete(inquiry);
    }

    @Test
    @DisplayName("문의사항 삭제 실패 - 작성자가 아닐 때 권한 예외 발생")
    void deleteInquiry_fail_forbidden() {
        // given
        Long inquiryId = 1L;
        Long ownerId = 100L;
        Long otherUserId = 200L;

        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Inquiry inquiry = Inquiry.createInquiry(owner, "제목", "내용", InquiryCategory.ERROR, "PC");

        given(inquiryRepository.findById(inquiryId)).willReturn(Optional.of(inquiry));

        // when & then
        assertThatThrownBy(() -> inquiryService.deleteInquiry(inquiryId, otherUserId, Role.ROLE_USER))
                .isInstanceOf(AccessDeniedException.class);
    }
}