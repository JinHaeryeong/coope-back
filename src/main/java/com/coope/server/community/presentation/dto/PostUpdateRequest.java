package com.coope.server.community.presentation.dto;

import com.coope.server.community.domain.post.enums.TechStack;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시글 수정 요청 DTO
 * 카테고리 변경은 허용하지 않으며, 제목, 내용 및 모집 카드 전용 필드만 수정 가능
 */
@Getter
@NoArgsConstructor
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    // 모집 카드 전용 수정 필드

    // 기술 스택 목록 (TechStack enum 값만 허용)
    private List<TechStack> techStacks;

    private Integer currentMembers;
    private Integer targetMembers;
}
