package com.coope.server.shared.ai;

import com.coope.server.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/usage")
@RequiredArgsConstructor
public class AiUsageController {

    private final AiUsageService aiUsageService;

    @GetMapping
    public ResponseEntity<Map<String, Integer>> getUsage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(Map.of(
                "CHAT", aiUsageService.getRemainingCount(userId, "CHAT", 5),
                "STT", aiUsageService.getRemainingCount(userId, "STT", 2)
        ));
    }
}
