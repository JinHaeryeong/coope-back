package com.coope.server.document.application.dto;

import com.coope.server.document.domain.Document;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DocumentResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String icon;
    private final String coverImage;
    private final Long parentId;
    private final boolean archived;
    private final boolean published;
    private final String lastEditedBy;
    private final boolean hasChildren;
    private final LocalDateTime createdAt;

    @Builder
    private DocumentResponse(Long id, String title, String content, String icon, String coverImage,
                              Long parentId, boolean archived, boolean published,
                              String lastEditedBy, boolean hasChildren, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.content = content;
        this.coverImage = coverImage;
        this.parentId = parentId;
        this.archived = archived;
        this.published = published;
        this.lastEditedBy = lastEditedBy;
        this.hasChildren = hasChildren;
        this.createdAt = createdAt;
    }

    public static DocumentResponse of(Document document, boolean hasChildren) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .icon(document.getIcon())
                .coverImage(document.getCoverImage())
                .parentId(document.getParentDocument() != null ? document.getParentDocument().getId() : null)
                .archived(document.isArchived())
                .published(document.isPublished())
                .lastEditedBy(document.getUser().getNickname())
                .hasChildren(hasChildren)
                .createdAt(document.getCreatedAt())
                .build();
    }

    public static DocumentResponse of(Document document, String content, boolean hasChildren) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(content)
                .icon(document.getIcon())
                .coverImage(document.getCoverImage())
                .parentId(document.getParentDocument() != null ? document.getParentDocument().getId() : null)
                .archived(document.isArchived())
                .published(document.isPublished())
                .lastEditedBy(document.getUser().getNickname())
                .hasChildren(hasChildren)
                .createdAt(document.getCreatedAt())
                .build();
    }
}
