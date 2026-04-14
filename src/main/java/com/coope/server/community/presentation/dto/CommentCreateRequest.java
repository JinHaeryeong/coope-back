package com.coope.server.community.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * 댓글 생성 요청 DTO
 * isPrivate = true 로 요청하면 비밀 댓글로 등록
 * 비밀 댓글은 게시글 작성자와 댓글 작성자 본인만 내용을 열람
 */
@Getter
public class CommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private boolean isPrivate;
}
