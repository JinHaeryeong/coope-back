package com.coope.server.auth.application;

import com.coope.server.auth.application.dto.FindEmailResponse;
import com.coope.server.shared.error.exception.BadRequestException;
import com.coope.server.shared.error.exception.UserNotFoundException;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.UserRepository;
import com.coope.server.user.domain.enums.Provider;
import com.coope.server.user.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountRecoveryServiceTest {

    @Mock UserRepository userRepository;
    @Mock MailService mailService;
    @Mock TokenService tokenService;
    @Mock LoginAttemptService loginAttemptService;

    @InjectMocks AccountRecoveryService accountRecoveryService;

    @Nested
    @DisplayName("findEmail()")
    class FindEmail {

        @Test
        @DisplayName("이름+닉네임 일치 시 마스킹된 이메일 목록 반환")
        void returnsMaskedEmailList() {
            User user = buildUser("홍길동", "길동이", "hong12@gmail.com", Provider.LOCAL);
            given(userRepository.findAllByNameAndNickname("홍길동", "길동이")).willReturn(List.of(user));

            List<FindEmailResponse> result = accountRecoveryService.findEmail("홍길동", "길동이");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getMaskedEmail()).isEqualTo("ho***@gmail.com");
            assertThat(result.get(0).getProvider()).isEqualTo("LOCAL");
        }

        @Test
        @DisplayName("이메일 앞자리가 2자 이하이면 첫 글자만 노출")
        void masksShortEmailPrefix() {
            User user = buildUser("홍길동", "길동이", "ab@gmail.com", Provider.LOCAL);
            given(userRepository.findAllByNameAndNickname("홍길동", "길동이")).willReturn(List.of(user));

            List<FindEmailResponse> result = accountRecoveryService.findEmail("홍길동", "길동이");

            assertThat(result.get(0).getMaskedEmail()).isEqualTo("a***@gmail.com");
        }

        @Test
        @DisplayName("소셜 계정도 provider 정보와 함께 반환")
        void returnsSocialAccountWithProvider() {
            User user = buildUser("홍길동", "길동이", "hong12@gmail.com", Provider.GOOGLE);
            given(userRepository.findAllByNameAndNickname("홍길동", "길동이")).willReturn(List.of(user));

            List<FindEmailResponse> result = accountRecoveryService.findEmail("홍길동", "길동이");

            assertThat(result.get(0).getProvider()).isEqualTo("GOOGLE");
        }

        @Test
        @DisplayName("일치하는 계정이 여러 개이면 전부 반환")
        void returnsMultipleAccounts() {
            User u1 = buildUser("홍길동", "길동이", "hong1@naver.com", Provider.LOCAL);
            User u2 = buildUser("홍길동", "길동이", "hong2@gmail.com", Provider.GOOGLE);
            given(userRepository.findAllByNameAndNickname("홍길동", "길동이")).willReturn(List.of(u1, u2));

            List<FindEmailResponse> result = accountRecoveryService.findEmail("홍길동", "길동이");

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("일치하는 계정이 없으면 UserNotFoundException")
        void throwsWhenNoMatch() {
            given(userRepository.findAllByNameAndNickname(anyString(), anyString())).willReturn(List.of());

            assertThatThrownBy(() -> accountRecoveryService.findEmail("없는사람", "없는닉네임"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("requestPasswordReset()")
    class RequestPasswordReset {

        @Test
        @DisplayName("로컬 계정 + 이름 일치 → 재설정 토큰 생성 및 메일 발송")
        void successForLocalUser() {
            User user = buildUser("홍길동", "길동이", "hong@gmail.com", Provider.LOCAL);
            given(userRepository.findByEmail("hong@gmail.com")).willReturn(Optional.of(user));
            given(tokenService.createResetToken("hong@gmail.com")).willReturn("reset-token-uuid");

            accountRecoveryService.requestPasswordReset("홍길동", "hong@gmail.com");

            verify(tokenService).createResetToken("hong@gmail.com");
            verify(mailService).send(eq("hong@gmail.com"), contains("비밀번호 재설정"), anyString());
        }

        @Test
        @DisplayName("이메일로 계정을 찾을 수 없으면 UserNotFoundException")
        void throwsWhenEmailNotFound() {
            given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

            assertThatThrownBy(() -> accountRecoveryService.requestPasswordReset("홍길동", "notfound@gmail.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("이름 불일치 → UserNotFoundException")
        void throwsWhenNameMismatch() {
            User user = buildUser("홍길동", "길동이", "hong@gmail.com", Provider.LOCAL);
            given(userRepository.findByEmail("hong@gmail.com")).willReturn(Optional.of(user));

            assertThatThrownBy(() -> accountRecoveryService.requestPasswordReset("다른이름", "hong@gmail.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("Google 소셜 계정 → BadRequestException (소셜 로그인 안내)")
        void throwsForGoogleUser() {
            User user = buildUser("홍길동", "길동이", "hong@gmail.com", Provider.GOOGLE);
            given(userRepository.findByEmail("hong@gmail.com")).willReturn(Optional.of(user));

            assertThatThrownBy(() -> accountRecoveryService.requestPasswordReset("홍길동", "hong@gmail.com"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Google");
        }

        @Test
        @DisplayName("카카오 소셜 계정 → BadRequestException (카카오 안내)")
        void throwsForKakaoUser() {
            User user = buildUser("홍길동", "길동이", "hong@kakao.com", Provider.KAKAO);
            given(userRepository.findByEmail("hong@kakao.com")).willReturn(Optional.of(user));

            assertThatThrownBy(() -> accountRecoveryService.requestPasswordReset("홍길동", "hong@kakao.com"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("카카오");
        }
    }

    @Nested
    @DisplayName("verifyUnlockAndIssueResetToken()")
    class VerifyUnlockAndIssueResetToken {

        @Test
        @DisplayName("유효한 unlockToken → 잠금 해제 + resetToken 반환")
        void unlocksAndReturnsResetToken() {
            given(tokenService.consumeUnlockToken("unlock-uuid")).willReturn("hong@gmail.com");
            given(tokenService.createResetToken("hong@gmail.com")).willReturn("reset-uuid");

            String resetToken = accountRecoveryService.verifyUnlockAndIssueResetToken("unlock-uuid");

            assertThat(resetToken).isEqualTo("reset-uuid");
            verify(loginAttemptService).unlock("hong@gmail.com");
        }
    }

    private User buildUser(String name, String nickname, String email, Provider provider) {
        User user = User.builder()
                .name(name)
                .nickname(nickname)
                .email(email)
                .provider(provider)
                .role(Role.ROLE_USER)
                .build();
        return user;
    }
}
