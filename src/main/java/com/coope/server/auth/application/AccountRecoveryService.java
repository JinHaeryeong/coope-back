package com.coope.server.auth.application;

import com.coope.server.auth.application.dto.FindEmailResponse;
import com.coope.server.shared.error.exception.BadRequestException;
import com.coope.server.shared.error.exception.UserNotFoundException;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.UserRepository;
import com.coope.server.user.domain.enums.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountRecoveryService {

    private final UserRepository userRepository;
    private final MailService mailService;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;

    @Value("${client.url}")
    private String clientUrl;

    public List<FindEmailResponse> findEmail(String name, String nickname) {
        List<User> users = userRepository.findAllByNameAndNickname(name, nickname);
        if (users.isEmpty()) throw new UserNotFoundException("입력하신 정보와 일치하는 계정을 찾을 수 없습니다.");
        return users.stream().map(FindEmailResponse::from).toList();
    }

    public void requestPasswordReset(String name, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("입력하신 정보와 일치하는 계정을 찾을 수 없습니다."));

        if (!user.getName().equals(name)) {
            throw new UserNotFoundException("입력하신 정보와 일치하는 계정을 찾을 수 없습니다.");
        }
        if (!Provider.LOCAL.equals(user.getProvider())) {
            String providerName = switch (user.getProvider()) {
                case GOOGLE -> "Google";
                case KAKAO  -> "카카오";
                case NAVER  -> "네이버";
                default     -> user.getProvider().name();
            };
            throw new BadRequestException(
                    providerName + " 소셜 로그인으로 가입된 계정입니다. " + providerName + "을 통해 로그인해 주세요.");
        }

        String resetToken = tokenService.createResetToken(email);
        sendPasswordResetEmail(email, resetToken);
        log.info("[AccountRecovery] 비밀번호 재설정 메일 발송: {}", email);
    }

    public void sendUnlockEmail(String email) {
        String unlockToken = tokenService.createUnlockToken(email);
        String unlockLink = clientUrl + "/reset-password?unlockToken=" + unlockToken;

        String content = """
                <html><body>
                <p>안녕하세요, Coope입니다.</p>
                <p>로그인 시도가 5회 초과되어 계정이 일시적으로 잠겼습니다.</p>
                <p>아래 버튼을 클릭하면 잠금이 해제되고 비밀번호를 재설정할 수 있습니다.</p>
                <p>(링크는 <strong>30분간</strong> 유효합니다.)</p>
                <br>%s
                <br><br>
                <p style="color:#888;font-size:12px;">본인이 요청하지 않은 경우 이 메일을 무시하세요.</p>
                </body></html>
                """.formatted(mailButton(unlockLink, "계정 잠금 해제 및 비밀번호 재설정"));

        mailService.send(email, "[Coope] 계정 잠금 해제 안내", content);
        log.info("[AccountRecovery] 잠금 해제 메일 발송: {}", email);
    }

    /** 잠금 해제 토큰 검증 => 잠금 해제 => 비밀번호 재설정 토큰 반환 */
    public String verifyUnlockAndIssueResetToken(String unlockToken) {
        String email = tokenService.consumeUnlockToken(unlockToken);
        loginAttemptService.unlock(email);

        String resetToken = tokenService.createResetToken(email);
        log.info("[AccountRecovery] 계정 잠금 해제 완료: {}", email);
        return resetToken;
    }

    public String verifyResetToken(String resetToken) {
        return tokenService.verifyResetToken(resetToken);
    }

    public void consumeResetToken(String resetToken) {
        tokenService.consumeResetToken(resetToken);
    }

    private void sendPasswordResetEmail(String email, String resetToken) {
        String resetLink = clientUrl + "/reset-password?resetToken=" + resetToken;

        String content = """
                <html><body>
                <p>안녕하세요, Coope입니다.</p>
                <p>비밀번호 재설정을 요청하셨습니다.</p>
                <p>아래 버튼을 클릭하여 비밀번호를 재설정해 주세요.</p>
                <p>(링크는 <strong>30분간</strong> 유효합니다.)</p>
                <br>%s
                <br><br>
                <p style="color:#888;font-size:12px;">본인이 요청하지 않은 경우 이 메일을 무시하세요.</p>
                </body></html>
                """.formatted(mailButton(resetLink, "비밀번호 재설정"));

        mailService.send(email, "[Coope] 비밀번호 재설정 안내", content);
    }

    private String mailButton(String link, String label) {
        return """
                <a href="%s" style="
                    display:inline-block;
                    padding:12px 24px;
                    background-color:#4F46E5;
                    color:#fff;
                    text-decoration:none;
                    border-radius:6px;
                    font-weight:bold;">%s</a>
                """.formatted(link, label);
    }
}
