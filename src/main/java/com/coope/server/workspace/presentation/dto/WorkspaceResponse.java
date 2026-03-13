package com.coope.server.workspace.presentation.dto;

import com.coope.server.workspace.domain.Workspace;
import com.coope.server.workspace.domain.enums.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceResponse {

    private final Long id;
    private final String name;
    private final WorkspaceRole role;
    private final String inviteCode;
    private final String status;

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
