package com.coope.server.domain.workspace.controller;

import com.coope.server.domain.workspace.dto.*;
import com.coope.server.domain.workspace.service.WorkspaceService;
import com.coope.server.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

        log.info("워크스페이스 생성 성공 - 생성자: {}, 이름: {}",
                userDetails.getUser().getNickname(), response.getName());

        log.debug("워크스페이스 초대코드 상세 - ID: {}, 초대코드: {}",
                response.getId(), response.getInviteCode());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<WorkspaceResponse> responses = workspaceService.getMyWorkspaces(userDetails.getUser().getId());

        log.info("워크스페이스 목록 조회 - 사용자: {}, 조회된 개수: {}",
                userDetails.getUser().getNickname(), responses.size());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{workspaceCode}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable("workspaceCode") String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        WorkspaceResponse response = workspaceService.getWorkspaceByCode(workspaceCode, userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{workspaceCode}")
    public ResponseEntity<WorkspaceResponse> update(
            @PathVariable("workspaceCode") String workspaceCode,
            @Valid @RequestBody WorkspaceWriteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        WorkspaceResponse response = workspaceService.updateWorkspaceName(workspaceCode, request.getName(), userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{workspaceCode}/members")
    public ResponseEntity<List<WorkspaceMemberResponse>> getWorkspaceMembers(
            @PathVariable("workspaceCode") String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // 모든 처리는 서비스에게 맡긴다!
        List<WorkspaceMemberResponse> responses =
                workspaceService.getWorkspaceMembers(workspaceCode, userDetails.getUser().getId());

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{workspaceId}/members/{targetUserId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable("workspaceId") Long workspaceId,
            @PathVariable("targetUserId") Long targetUserId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        workspaceService.updateMemberRole(workspaceId, targetUserId, request.getRole(), userDetails.getUser());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{workspaceCode}")
    public ResponseEntity<Void> delete(
            @PathVariable("workspaceCode") String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        workspaceService.deleteWorkspace(workspaceCode, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    public ResponseEntity<WorkspaceResponse> join(
            @Valid @RequestBody WorkspaceJoinRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        WorkspaceResponse response = workspaceService.joinWorkspace(request.getInviteCode(), userDetails.getUser());

        log.info("워크스페이스 참여 성공 - 사용자: {}, 워크스페이스 이름: {}",
                userDetails.getUser().getNickname(), response.getName());

        return ResponseEntity.ok(response);
    }
}