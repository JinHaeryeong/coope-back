package com.coope.server.domain.comment.dto;

import com.coope.server.domain.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String author;
    private Long userId;
    private String authorImgUrl;
    private String imageUrl;
    private String createdAt;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getUser().getNickname())
                .userId(comment.getUser().getId())
                .authorImgUrl(comment.getUser().getUserIcon())
                .imageUrl(comment.getImageUrl())
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }
}