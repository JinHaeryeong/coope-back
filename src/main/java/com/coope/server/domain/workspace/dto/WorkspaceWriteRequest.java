package com.coope.server.domain.workspace.dto;

import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.entity.Workspace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 빈 생성자 막기
public class WorkspaceWriteRequest {

    @NotBlank(message = "워크스페이스 이름은 필수입니다.")
    @Size(max = 20, message = "이름은 20자 이내로 입력해주세요.")
    private String name;

    public WorkspaceWriteRequest(String name) {
        this.name = name;
    }

    public Workspace toEntity(User user, String inviteCode) {
        return Workspace.builder()
                .name(this.name)
                .creator(user)
                .inviteCode(inviteCode)// 생성자 정보 매핑
                .build();
    }
}