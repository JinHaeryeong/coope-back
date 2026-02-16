package com.coope.server.domain.document.dto;

import com.coope.server.domain.document.entity.Document;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DocumentResponse {
    private final Long id;
    private final String title;
    private final String icon;
    private final String coverImage;
    private final Long parentId;
    private final boolean archived;
    private final boolean published;
    private final String lastEditedBy;
    private final boolean hasChildren;

    @Builder
    private DocumentResponse(Long id, String title, String icon, String coverImage,
                             Long parentId, boolean archived, boolean published,
                             String lastEditedBy, boolean hasChildren) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.coverImage = coverImage;
        this.parentId = parentId;
        this.archived = archived;
        this.published = published;
        this.lastEditedBy = lastEditedBy;
        this.hasChildren = hasChildren;
    }

    public static DocumentResponse of(Document document, boolean hasChildren) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .icon(document.getIcon())
                .coverImage(document.getCoverImage())
                .parentId(document.getParentDocument() != null ? document.getParentDocument().getId() : null)
                .archived(document.isArchived())
                .published(document.isPublished())
                .lastEditedBy(document.getUser().getNickname())
                .hasChildren(hasChildren)
                .build();
    }
}