package com.coope.server.domain.document.dto;

import com.coope.server.domain.document.entity.Document;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentResponse {
    private Long id;
    private String title;
    private String icon;
    private String coverImage;
    private Long parentId;
    private boolean archived;
    private boolean published;
    private String lastEditedBy;
    private boolean hasChildren;

    public static DocumentResponse from(Document document, boolean hasChildren) {
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