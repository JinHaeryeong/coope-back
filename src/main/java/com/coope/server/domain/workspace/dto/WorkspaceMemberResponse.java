package com.coope.server.domain.workspace.dto;

import com.coope.server.domain.workspace.enums.WorkspaceRole;
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
