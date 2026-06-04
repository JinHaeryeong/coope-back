package com.coope.server.community.presentation.dto;

import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.enums.PostCategory;
import com.coope.server.community.domain.post.enums.TechStack;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 상세 응답 DTO
 */
@Getter
@Builder
public class PostDetailResponse {

    private Long id;
    private PostCategory category;
    private String title;
    private String content;

    // 모집 카드 전용 필드
    private List<TechStack> techStacks;
    private Integer currentMembers;
    private Integer targetMembers;

    private String authorNickname;
    private String authorIcon;
    private int viewCount;
    private int likeCount;

    @JsonProperty("isLiked")
    private boolean isLiked;

    private LocalDateTime createdAt;

    private List<CommentResponse> comments;

    public static PostDetailResponse from(Post post, List<CommentResponse> comments, boolean isLiked) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .techStacks(post.getTechStackValues())
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
