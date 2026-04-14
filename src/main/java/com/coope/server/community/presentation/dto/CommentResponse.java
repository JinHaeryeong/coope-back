package com.coope.server.community.presentation.dto;

import com.coope.server.community.domain.comment.PostComment;
import com.coope.server.user.domain.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 댓글 응답 DTO
 * 비밀 댓글 처리 정책:
 * 열람 권한이 없는 사용자에게는 content를 "비밀 댓글입니다."로 마스킹하고
 * isPrivate 플래그를 함께 내려보내어 클라이언트가 잠금 아이콘 등 UI를 표현할 수 있게 함
 */
@Getter
@Builder
public class CommentResponse {

    private Long id;
    private String content;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    @JsonProperty("isMasked")
    private boolean isMasked;

    private String authorNickname;
    private String authorIcon;
    private LocalDateTime createdAt;

    /** 비밀 댓글 마스킹 문자열 상수 */
    private static final String MASKED_CONTENT = "비밀 댓글입니다.";

    /**
     * 댓글 엔티티를 응답 DTO로 변환
     *
     * @param comment 댓글 엔티티
     * @param viewer  현재 요청 사용자 (권한 판단에 사용)
     */
    public static CommentResponse from(PostComment comment, User viewer) {
        boolean readable = comment.isReadableBy(viewer);

        return CommentResponse.builder()
                .id(comment.getId())
                // 열람 불가 시 내용 마스킹
                .content(readable ? comment.getContent() : MASKED_CONTENT)
                .isPrivate(comment.isPrivate())
                .isMasked(!readable)
                .authorNickname(comment.getAuthor().getNickname())
                .authorIcon(comment.getAuthor().getUserIcon())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
