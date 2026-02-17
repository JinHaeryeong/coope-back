package com.coope.server.domain.ai.controller;

import com.coope.server.domain.ai.dto.VoiceProcessResponse;
import com.coope.server.domain.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/process-voice")
    public ResponseEntity<VoiceProcessResponse> processVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("workspaceCode") String workspaceCode) {

        // AI 처리 (STT + Summary)
        VoiceProcessResponse result = aiService.processVoice(file);

        return ResponseEntity.ok(result);
    }
}