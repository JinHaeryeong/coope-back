package com.coope.server.workspace.presentation;

import com.coope.server.workspace.application.WorkspaceService;
import com.coope.server.workspace.presentation.dto.*;
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workspaceService.createWorkspace(request.getName(), userDetails.getUser()));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> getMyWorkspaces(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(workspaceService.getMyWorkspaces(userDetails.getUser().getId()));
    }

    @GetMapping("/{workspaceCode}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(workspaceService.getWorkspaceByCode(workspaceCode, userDetails.getUser()));
    }

    @PatchMapping("/{workspaceCode}")
    public ResponseEntity<WorkspaceResponse> update(
            @PathVariable String workspaceCode,
            @Valid @RequestBody WorkspaceWriteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                workspaceService.updateWorkspaceName(workspaceCode, request.getName(), userDetails.getUser()));
    }

    @GetMapping("/{workspaceCode}/members")
    public ResponseEntity<List<WorkspaceMemberResponse>> getWorkspaceMembers(
            @PathVariable String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                workspaceService.getWorkspaceMembers(workspaceCode, userDetails.getUser().getId()));
    }

    @PatchMapping("/{workspaceCode}/members/{targetUserId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @PathVariable String workspaceCode,
            @PathVariable Long targetUserId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        workspaceService.updateMemberRoleByCode(workspaceCode, targetUserId, request.getRole(), userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{workspaceCode}")
    public ResponseEntity<Void> delete(
            @PathVariable String workspaceCode,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        workspaceService.deleteWorkspace(workspaceCode, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    public ResponseEntity<WorkspaceResponse> join(
            @Valid @RequestBody WorkspaceJoinRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
                workspaceService.joinWorkspace(request.getInviteCode(), userDetails.getUser()));
    }
}
