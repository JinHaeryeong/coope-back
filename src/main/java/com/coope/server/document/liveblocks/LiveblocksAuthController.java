package com.coope.server.document.liveblocks;

import com.coope.server.shared.security.UserDetailsImpl;
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
