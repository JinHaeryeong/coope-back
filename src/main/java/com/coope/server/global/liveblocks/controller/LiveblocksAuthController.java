package com.coope.server.global.liveblocks.controller;

import com.coope.server.global.liveblocks.service.LiveblocksAuthService;
import com.coope.server.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LiveblocksAuthController {

    private final LiveblocksAuthService liveblocksAuthService;

    @PostMapping("/liveblocks-auth")
    public ResponseEntity<Map<String, String>> authorize(
            @RequestBody Map<String, String> requestBody,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        String token = liveblocksAuthService.getAuthorizedToken(
                userDetails.getUser(),
                requestBody.get("roomId")
        );

        return ResponseEntity.ok(Map.of("token", token));
    }
}