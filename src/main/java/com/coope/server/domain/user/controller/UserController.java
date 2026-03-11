package com.coope.server.domain.user.controller;

import com.coope.server.domain.user.dto.*;
import com.coope.server.domain.user.service.UserService;
import com.coope.server.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SignupResponse> signup(@Valid @ModelAttribute SignupRequest request) {
        Long userId = userService.signup(request);
        log.info("회원가입 성공 - 유저 ID: {}, 이메일: {}", userId, request.getEmail());
        return ResponseEntity.ok(SignupResponse.success(request.getEmail()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getMyInfo(userDetails.getUser().getId()));
    }

    @PostMapping("/me/verify-password")
    public ResponseEntity<Void> verifyPassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody PasswordCheckRequest request
    ) {
        userService.checkPassword(userDetails.getUser().getId(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    @PatchMapping(value = "/me", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @ModelAttribute ProfileUpdateFullRequest request
    ) {
        UserResponse response = userService.updateProfile(
                userDetails.getUser().getId(),
                request
        );
        log.info("유저 프로필 수정 완료 - 유저 ID: {}", userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<UserSearchResponse> searchUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Pattern(
                    regexp = "^[a-zA-Z0-9가-힣 ]{2,20}$",
                    message = "닉네임은 특수문자를 제외한 2~20자 이내여야 합니다."
            )
            @RequestParam String nickname) {
        UserSearchResponse response = userService.searchUserByNickname(
                userDetails.getUser().getId(),
                nickname
        );
        return ResponseEntity.ok(response);
    }
}