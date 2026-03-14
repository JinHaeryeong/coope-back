package com.coope.server.auth.presentation;

import com.coope.server.auth.application.AccountRecoveryService;
import com.coope.server.auth.application.AuthService;
import com.coope.server.auth.application.EmailAuthService;
import com.coope.server.auth.presentation.dto.*;
import com.coope.server.shared.config.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Auth", description = "인증/인가 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmailAuthService emailAuthService;
    private final AccountRecoveryService accountRecoveryService;
    private final JwtProperties jwtProperties;

    @Operation(summary = "로그인",
            description = "이메일/비밀번호로 로그인합니다. 5회 실패 시 계정이 잠기고 잠금 해제 메일이 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 — AccessToken 반환, RefreshToken 쿠키 설정"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치"),
            @ApiResponse(responseCode = "423", description = "계정 잠금 (로그인 5회 초과)")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        LoginResponse loginResponse = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(loginResponse.getRefreshToken()).toString());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "로그아웃",
            description = "AccessToken 블랙리스트 등록 및 RefreshToken 삭제. refreshToken 쿠키를 만료시킵니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        authService.logout(accessToken, refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "AccessToken 재발급",
            description = "쿠키의 refreshToken으로 새 AccessToken을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 RefreshToken")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        return ResponseEntity.ok(Map.of("accessToken", authService.refresh(refreshToken)));
    }

    @Operation(summary = "이메일 인증 코드 발송",
            description = "회원가입 시 이메일로 6자리 인증 코드를 발송합니다. 유효 시간 5분.")
    @ApiResponse(responseCode = "200", description = "발송 성공")
    @PostMapping("/email/send")
    public ResponseEntity<Void> sendEmail(@Valid @RequestBody EmailRequest request) {
        emailAuthService.sendAuthCode(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 인증 코드 확인",
            description = "발송된 인증 코드가 일치하면 해당 이메일을 인증 완료 처리합니다. (10분간 유효)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 성공"),
            @ApiResponse(responseCode = "400", description = "인증 코드 불일치 또는 만료")
    })
    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        emailAuthService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "아이디(이메일) 찾기",
            description = "이름 + 닉네임으로 가입된 계정을 조회합니다. 이메일은 앞 2자리만 노출됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "일치하는 계정 없음")
    })
    @PostMapping("/find-email")
    public ResponseEntity<List<FindEmailResponse>> findEmail(
            @Valid @RequestBody FindEmailRequest request) {
        return ResponseEntity.ok(accountRecoveryService.findEmail(request.getName(), request.getNickname()));
    }

    @Operation(summary = "비밀번호 찾기 — 재설정 메일 발송",
            description = "이름 + 이메일로 계정을 확인 후 비밀번호 재설정 링크를 메일로 발송합니다. 소셜 계정은 사용 불가.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메일 발송 성공"),
            @ApiResponse(responseCode = "400", description = "소셜 로그인 계정"),
            @ApiResponse(responseCode = "404", description = "일치하는 계정 없음")
    })
    @PostMapping("/find-password")
    public ResponseEntity<Void> findPassword(
            @Valid @RequestBody FindPasswordRequest request) {
        accountRecoveryService.requestPasswordReset(request.getName(), request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "계정 잠금 해제",
            description = "메일로 받은 unlockToken을 검증하고 계정 잠금을 해제합니다. 비밀번호 재설정용 resetToken을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "잠금 해제 성공 — resetToken 반환"),
            @ApiResponse(responseCode = "401", description = "만료되거나 유효하지 않은 토큰")
    })
    @PostMapping("/unlock")
    public ResponseEntity<Map<String, String>> unlockAccount(@RequestParam String unlockToken) {
        String resetToken = accountRecoveryService.verifyUnlockAndIssueResetToken(unlockToken);
        return ResponseEntity.ok(Map.of("resetToken", resetToken));
    }

    @Operation(summary = "비밀번호 재설정",
            description = "resetToken(잠금해제 또는 비밀번호찾기에서 발급)으로 비밀번호를 변경합니다. 토큰은 일회용입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재설정 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "만료되거나 유효하지 않은 resetToken")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    private ResponseCookie buildRefreshCookie(String value) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .path("/")
                .maxAge(jwtProperties.getRefreshTokenExpiration() / 1000)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(jwtProperties.isCookieSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
