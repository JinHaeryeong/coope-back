package com.coope.server.domain.inquiry.service;

import com.coope.server.inquiry.application.dto.InquiryCreateRequest;
import com.coope.server.inquiry.domain.Inquiry;
import com.coope.server.inquiry.domain.enums.InquiryCategory;
import com.coope.server.inquiry.domain.InquiryRepository;
import com.coope.server.inquiry.application.InquiryService;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.enums.Role;
import com.coope.server.user.domain.UserRepository;
import com.coope.server.shared.error.exception.AccessDeniedException;
import com.coope.server.shared.file.FileService;
import com.coope.server.shared.file.ImageCategory;
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

import static org.assertj.core.api.Assertions.assertThat;
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
        User user = User.builder().nickname("테스터").build();
        ReflectionTestUtils.setField(user, "id", userId);

        MockMultipartFile file = new MockMultipartFile("files", "test.jpg", "image/jpeg", "data".getBytes());
        InquiryCreateRequest request = InquiryCreateRequest.builder()
                .title("문의 제목")
                .content("문의 내용")
                .category(InquiryCategory.ERROR)
                .environment("Android")
                .files(List.of(file))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(fileService.upload(any(), eq(ImageCategory.INQUIRY))).willReturn("http://s3-url.com/test.jpg");

        // save 시 ID를 가진 객체가 반환되도록 설정
        Inquiry savedInquiry = Inquiry.createInquiry(user, "제목", "내용", InquiryCategory.ERROR, "Android");
        ReflectionTestUtils.setField(savedInquiry, "id", 100L);
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(savedInquiry);

        // when
        Long inquiryId = inquiryService.createInquiry(
                userId,
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getEnvironment(),
                request.getFiles()
        );

        // then
        assertThat(inquiryId).isEqualTo(100L);
        verify(fileService, times(1)).upload(any(), eq(ImageCategory.INQUIRY));
        verify(inquiryRepository).save(any(Inquiry.class));
    }

    @Test
    @DisplayName("문의사항 삭제 성공 - 작성자 본인일 때")
    void deleteInquiry_success_by_owner() {
        // given
        Long inquiryId = 1L;
        Long userId = 100L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        Inquiry inquiry = Inquiry.createInquiry(user, "제목", "내용", InquiryCategory.ERROR, "PC");
        given(inquiryRepository.findById(inquiryId)).willReturn(Optional.of(inquiry));

        // when
        inquiryService.deleteInquiry(inquiryId, userId, Role.ROLE_USER);

        // then
        assertThat(inquiry.getDeletedAt()).isNotNull();

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