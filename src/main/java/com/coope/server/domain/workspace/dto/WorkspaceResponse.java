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