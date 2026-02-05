package com.coope.server.domain.notice.dto;

import com.coope.server.domain.notice.entity.Notice;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeResponse {
    private Long id;
    private String title;
    private String author; // "관리자" 고정
    private int views;
    private LocalDateTime createdAt;

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