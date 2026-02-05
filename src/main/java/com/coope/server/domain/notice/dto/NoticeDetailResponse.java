package com.coope.server.domain.notice.dto;


import com.coope.server.domain.notice.entity.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String imageUrl;
    private Integer views;
    private LocalDateTime createdAt;

    // Entity -> DTO 변환 메서드 (정적 팩토리 메서드 패턴)
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
}
