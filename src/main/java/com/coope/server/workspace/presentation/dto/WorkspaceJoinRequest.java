package com.coope.server.workspace.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 파싱을 위한 기본 생성자
@AllArgsConstructor
public class WorkspaceJoinRequest {

    @NotBlank(message = "초대 코드는 필수입니다.")
    private String inviteCode;
}