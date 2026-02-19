package com.coope.server.domain.workspace.dto;

import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.entity.Workspace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkspaceWriteRequest {

    @NotBlank(message = "워크스페이스 이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이내로 입력해주세요.")
    private String name;

    public Workspace toEntity(User user, String inviteCode) {
        return Workspace.builder()
                .name(this.name)
                .creator(user)
                .inviteCode(inviteCode)// 생성자 정보 매핑
                .build();
    }
}