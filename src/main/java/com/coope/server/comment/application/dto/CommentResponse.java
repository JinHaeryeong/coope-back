package com.coope.server.comment.application.dto;

import com.coope.server.comment.domain.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CommentResponse {
    private final Long id;
    private final String content;
    private final String author;
    private final Long userId;
    private final String authorImgUrl;
    private final String imageUrl;
    private final String createdAt;

    @Builder
    private CommentResponse(Long id, String content, String author, Long userId,
                            String authorImgUrl, String imageUrl, String createdAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.userId = userId;
        this.authorImgUrl = authorImgUrl;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

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
