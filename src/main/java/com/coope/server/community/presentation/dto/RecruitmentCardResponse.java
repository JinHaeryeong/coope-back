package com.coope.server.community.presentation.dto;

import com.coope.server.community.domain.post.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 모집 카드(Recruitment Card) UI 전용 응답 DTO
 * 프론트엔드에서 카드 형태의 UI를 렌더링할 때 사용하는 DTO
 * 일반 게시글 목록 DTO(PostResponse)와 별도로 분리하여
 * 모집 카드에 특화된 필드
 */
@Getter
@Builder
public class RecruitmentCardResponse {

    private Long id;
    private String title;

    // 프로젝트 소개 요약 (content 앞 100자)
    private String summary;

    // 사용 기술 스택
    private String techStack;

    // 현재 참여 인원
    private int currentMembers;

    // 목표 참여 인원
    private int targetMembers;

    // 작성자 닉네임
    private String authorNickname;

    // 작성자 프로필 이미지
    private String authorIcon;

    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;

    public static RecruitmentCardResponse from(Post post) {
        // content가 100자를 초과하면 잘라서 요약으로 사용
        String summary = post.getContent().length() > 100
                ? post.getContent().substring(0, 100) + "..."
                : post.getContent();

        return RecruitmentCardResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .summary(summary)
                .techStack(post.getTechStack())
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
