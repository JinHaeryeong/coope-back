package com.coope.server.domain.notice.dto;


import com.coope.server.domain.notice.entity.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeDetailResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final String imageUrl;
    private final Integer views;
    private final LocalDateTime createdAt;

    @Builder
    private NoticeDetailResponse(Long id, String title, String content, String author,
                                 String imageUrl, Integer views, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.imageUrl = imageUrl;
        this.views = views;
        this.createdAt = createdAt;
    }

    public static NoticeDetailResponse from(Notice notice) {
        return NoticeDetailResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrl(notice.getImageUrl())
                .author("관리자")
                .views(notice.getViews())
                .createdAt(notice.getCreatedAt())
                .build();
    }

    public static NoticeDetailResponse from(Notice notice, int redisViews) {
        return NoticeDetailResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrl(notice.getImageUrl())
                .author("관리자")
                .views(notice.getViews() + redisViews)
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
