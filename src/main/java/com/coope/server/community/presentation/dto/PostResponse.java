package com.coope.server.community.presentation.dto;

import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.enums.PostCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 게시글 목록 응답 DTO
 * 목록 조회에 필요한 요약 정보만 포함
 */
@Getter
@Builder
public class PostResponse {

    private Long id;
    private PostCategory category;
    private String title;
    private String authorNickname;
    private String authorIcon;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;

    // 모집 카드 전용 요약 필드
    private String techStack;
    private Integer currentMembers;
    private Integer targetMembers;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .authorNickname(post.getAuthor().getNickname())
                .authorIcon(post.getAuthor().getUserIcon())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .techStack(post.getTechStack())
                .currentMembers(post.getCurrentMembers())
                .targetMembers(post.getTargetMembers())
                .build();
    }
}
