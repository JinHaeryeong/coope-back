package com.coope.server.domain.notice.dto;

import com.coope.server.domain.notice.entity.Notice;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class NoticeResponse {
    private final Long id;
    private final String title;
    private final String author;
    private final int views;
    private final LocalDateTime createdAt;

    @Builder
    private NoticeResponse(Long id, String title, String author, int views, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.views = views;
        this.createdAt = createdAt;
    }

    public static NoticeResponse from(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .author("관리자")
                .views(notice.getViews())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}