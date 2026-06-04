package com.coope.server.community.presentation.dto;

import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.enums.TechStack;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 모집 카드(Recruitment Card) UI 전용 응답 DTO
 */
@Getter
@Builder
public class RecruitmentCardResponse {

    private Long id;
    private String title;

    // 프로젝트 소개 요약 (content 앞 100자)
    private String summary;

    // 기술 스택 목록
    private List<TechStack> techStacks;

    private int currentMembers;
    private int targetMembers;

    private String authorNickname;
    private String authorIcon;

    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;

    public static RecruitmentCardResponse from(Post post) {
        String summary = post.getContent().length() > 100
                ? post.getContent().substring(0, 100) + "..."
                : post.getContent();

        return RecruitmentCardResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .summary(summary)
                .techStacks(post.getTechStackValues())
                .currentMembers(post.getCurrentMembers() != null ? post.getCurrentMembers() : 0)
                .targetMembers(post.getTargetMembers() != null ? post.getTargetMembers() : 0)
                .authorNickname(post.getAuthor().getNickname())
                .authorIcon(post.getAuthor().getUserIcon())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
