package com.coope.server.domain.user.service;

import com.coope.server.domain.auth.service.EmailAuthService;
import com.coope.server.domain.friend.service.FriendService;
import com.coope.server.domain.user.dto.ProfileUpdateFullRequest;
import com.coope.server.domain.user.dto.UserResponse;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.error.exception.AuthenticationException; // 🚀 패키지 경로 확인!
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private FileService fileService;


    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("프로필 수정 성공 - 닉네임, 이미지, 비밀번호 모두 변경")
    void updateProfile_success() throws Exception {
        Long userId = 1L;
        User user = User.builder()
                .email("test@coope.com")
                .nickname("oldNickname")
                .userIcon("old-icon.jpg")
                .password("encodedOldPassword")
                .build();

        MockMultipartFile newImage = new MockMultipartFile("profileImage", "new.jpg", "image/jpeg", "test".getBytes());
        ProfileUpdateFullRequest request = new ProfileUpdateFullRequest();
        request.setNickname("newNickname");
        request.setProfileImage(newImage);
        request.setCurrentPassword("oldPassword123!");
        request.setNewPassword("newPassword123!");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("newNickname")).willReturn(false);
        given(fileService.upload(any(), eq(ImageCategory.PROFILE))).willReturn("new-icon-url.jpg");
        given(passwordEncoder.matches("oldPassword123!", "encodedOldPassword")).willReturn(true);
        given(passwordEncoder.encode("newPassword123!")).willReturn("encodedNewPassword");

        // when
        UserResponse response = userService.updateProfile(userId, request);

        // then
        assertThat(response.getNickname()).isEqualTo("newNickname");
        assertThat(user.getUserIcon()).isEqualTo("new-icon-url.jpg");

        verify(fileService).deleteFile("old-icon.jpg", ImageCategory.PROFILE);
    }

    @Test
    @DisplayName("프로필 수정 실패 - 현재 비밀번호 불일치")
    void updateProfile_fail_password_mismatch() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .password("encodedOldPassword")
                .build();

        ProfileUpdateFullRequest request = new ProfileUpdateFullRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123!");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(userId, request))
                .isInstanceOf(AuthenticationException.class);
    }
}