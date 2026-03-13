package com.coope.server.workspace.presentation.dto;

import com.coope.server.workspace.domain.enums.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceMemberResponse {
    private Long userId;
    private String nickname;
    private WorkspaceRole role;
}
