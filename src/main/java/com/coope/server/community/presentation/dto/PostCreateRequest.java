package com.coope.server.community.presentation.dto;

import com.coope.server.community.domain.post.enums.PostCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 생성 요청 DTO
 * 카테고리가 RECRUITMENT인 경우 techStack, currentMembers, targetMembers 필드가
 * 서비스 계층에서 추가로 검증
 */
@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private PostCategory category;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    // 모집 카드 전용 필드 (RECRUITMENT 카테고리일 때만 사용)

    // 사용 기술 스택
    private String techStack;

    // 현재 참여 인원
    @Min(value = 1, message = "현재 인원은 1명 이상이어야 합니다.")
    private Integer currentMembers;

    // 목표 참여 인원
    @Max(value = 100, message = "목표 인원은 100명 이하여야 합니다.")
    private Integer targetMembers;
}
