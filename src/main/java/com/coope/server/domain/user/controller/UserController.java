package com.coope.server.domain.user.controller;

import com.coope.server.domain.user.dto.SignupRequest;
import com.coope.server.domain.user.dto.SignupResponse;
import com.coope.server.domain.user.dto.UserResponse;
import com.coope.server.domain.user.service.UserService;
import com.coope.server.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ResponseEntity<SignupResponse> signup(@Valid @ModelAttribute SignupRequest request) {
        Long userId = userService.signup(request);
        log.info("회원가입 성공 - 유저 ID: {}, 이메일: {}", userId, request.getEmail());
        // 성공 응답 반환
        return ResponseEntity.ok(SignupResponse.success(request.getEmail()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.getMyInfo(userDetails.getUser().getId()));
    }


    @GetMapping("/search")
    public ResponseEntity<UserResponse> searchUser(@RequestParam String nickname) {
        UserResponse response = userService.searchUserByNickname(nickname);
        return ResponseEntity.ok(response);
    }
}