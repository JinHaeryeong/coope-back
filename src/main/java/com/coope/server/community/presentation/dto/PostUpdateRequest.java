package com.coope.server.community.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 게시글 수정 요청 DTO
 * 카테고리 변경은 허용하지 않으며, 제목, 내용 및 모집 카드 전용 필드만 수정 가능
 */
@Getter
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    // 모집 카드 전용 수정 필드

    private String techStack;
    private Integer currentMembers;
    private Integer targetMembers;
}
