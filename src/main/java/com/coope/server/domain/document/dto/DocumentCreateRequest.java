package com.coope.server.domain.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private Long parentId;

    @NotBlank(message = "워크스페이스 코드는 필수입니다.")
    private String workspaceCode;

    private String icon;

    private String content;
}