package com.coope.server.domain.workspace.dto;

import com.coope.server.domain.workspace.enums.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberRoleUpdateRequest {
    @NotNull(message = "변경할 권한(role)은 필수입니다.")
    private WorkspaceRole role;
}