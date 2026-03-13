package com.coope.server.domain.ai.controller;

import com.coope.server.domain.ai.dto.VoiceProcessResponse;
import com.coope.server.domain.ai.service.AiService;
import com.coope.server.domain.workspace.entity.Workspace;
import com.coope.server.domain.workspace.service.WorkspaceService;
import com.coope.server.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {
    private final AiService aiService;
    private final WorkspaceService workspaceService;

    @PostMapping("/process-voice")
    public ResponseEntity<VoiceProcessResponse> processVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam("workspaceCode") String workspaceCode,
    @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("AI 요청 User: {}, Workspace: {}, File: {}",
                userDetails.getUsername(), workspaceCode, file.getOriginalFilename());
        Workspace workspace = workspaceService.getByInviteCode(workspaceCode);

        workspace.validateMember(userDetails.getUser().getId());

        VoiceProcessResponse result = aiService.processVoice(file);

        log.info("AI 요청 성공 : {}", workspaceCode);
        return ResponseEntity.ok(result);
    }
}