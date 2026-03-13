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
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(
            @Valid @RequestBody WorkspaceWriteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        WorkspaceResponse response = workspaceService.createWorkspace(request, userDetails.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<WorkspaceResponse> responses = workspaceService.getMyWorkspaces(userDetails.getUser().getId());
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

        List<WorkspaceMemberResponse> responses =
                workspaceService.getWorkspaceMembers(workspaceCode, userDetails.getUser().getId());

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{workspaceCode}/members/{targetUserId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable("workspaceCode") String workspaceCode,
            @PathVariable("targetUserId") Long targetUserId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        workspaceService.updateMemberRoleByCode(workspaceCode, targetUserId, request.getRole(), userDetails.getUser());

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
        return ResponseEntity.ok(response);
    }
}