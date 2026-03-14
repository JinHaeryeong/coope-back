package com.coope.server.domain.user.service;

import com.coope.server.user.presentation.dto.ProfileUpdateFullRequest;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.enums.Provider;
import com.coope.server.user.domain.UserRepository;
import com.coope.server.shared.error.exception.AuthenticationException;
import com.coope.server.user.application.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;


    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("프로필 수정 성공 - 닉네임과 이미지만 변경")
    void updateProfile_only_nickname_and_image() {
        // given
        User user = User.builder().nickname("old").userIcon("old.jpg").build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        ProfileUpdateFullRequest request = new ProfileUpdateFullRequest();
        request.setNickname("newNick");

        // when
        userService.updateProfile(1L, request);

        // then
        assertThat(user.getNickname()).isEqualTo("newNick");
        verify(passwordEncoder, never()).encode(anyString()); // 비번 암호화가 호출되면 안 됨!
    }

    @Test
    @DisplayName("프로필 수정 성공 - 비밀번호까지 전체 변경")
    void updateProfile_full_success() {
        // given
        User user = User.builder()
                .password("encodedOld")
                .provider(Provider.LOCAL)
                .build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("currentRaw", "encodedOld")).willReturn(true);
        given(passwordEncoder.encode("newRaw")).willReturn("encodedNew");

        ProfileUpdateFullRequest request = new ProfileUpdateFullRequest();
        request.setCurrentPassword("currentRaw");
        request.setNewPassword("newRaw");

        // when
        userService.updateProfile(1L, request);

        // then
        assertThat(user.getPassword()).isEqualTo("encodedNew");
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