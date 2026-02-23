package com.coope.server.domain.workspace.dto;

import com.coope.server.domain.workspace.entity.Workspace;
import com.coope.server.domain.workspace.enums.WorkspaceRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceResponse {

    private Long id;
    private String name;
    private WorkspaceRole role;
    private String inviteCode;
    private String status;

    public static WorkspaceResponse from(Workspace workspace, WorkspaceRole role, String status) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .role(role)
                .inviteCode(workspace.getInviteCode())
                .status(status)
                .build();
    }

    public static WorkspaceResponse from(Workspace workspace, WorkspaceRole role) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .role(role)
                .inviteCode(workspace.getInviteCode())
                .build();
    }
}