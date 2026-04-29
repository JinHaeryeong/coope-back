package com.coope.server.community.presentation.dto;

import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.enums.PostCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 응답 DTO
 * 게시글의 전체 내용과 댓글 목록을 함께 반환
 * 댓글 목록은 현재 사용자의 권한에 따라 비밀 댓글이 마스킹된 상태로 포함
 */
@Getter
@Builder
public class PostDetailResponse {

    private Long id;
    private PostCategory category;
    private String title;
    private String content;

    // 모집 카드 전용 필드
    private String techStack;
    private Integer currentMembers;
    private Integer targetMembers;

    private String authorNickname;
    private String authorIcon;
    private int viewCount;
    private int likeCount;

    @JsonProperty("isLiked")
    private boolean isLiked;

    private LocalDateTime createdAt;

    // 댓글 목록 (비밀 댓글 마스킹 적용됨)
    private List<CommentResponse> comments;

    public static PostDetailResponse from(Post post, List<CommentResponse> comments, boolean isLiked) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .techStack(post.getTechStack())
                .currentMembers(post.getCurrentMembers())
                .targetMembers(post.getTargetMembers())
                .authorNickname(post.getAuthor().getNickname())
                .authorIcon(post.getAuthor().getUserIcon())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .comments(comments)
                .build();
    }
}
