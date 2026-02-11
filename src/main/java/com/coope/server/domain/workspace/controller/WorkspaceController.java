package com.coope.server.domain.workspace.controller;

import com.coope.server.domain.workspace.dto.WorkspaceResponse;
import com.coope.server.domain.workspace.dto.WorkspaceWriteRequest;
import com.coope.server.domain.workspace.service.WorkspaceService;
import com.coope.server.global.security.UserDetailsImpl; //
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; //
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Slf4j //
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(
            @Valid @RequestBody WorkspaceWriteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        WorkspaceResponse response = workspaceService.createWorkspace(request, userDetails.getUser());

        log.info("워크스페이스 생성 성공 - 생성자: {}, 초대코드: {}",
                userDetails.getUser().getNickname(), response.getInviteCode());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(
            @AuthenticationPrincipal UserDetailsImpl userDetails) { //

        List<WorkspaceResponse> responses = workspaceService.getMyWorkspaces(userDetails.getUser().getId());

        log.info("워크스페이스 목록 조회 - 사용자: {}, 조회된 개수: {}",
                userDetails.getUser().getNickname(), responses.size());

        return ResponseEntity.ok(responses);
    }
}