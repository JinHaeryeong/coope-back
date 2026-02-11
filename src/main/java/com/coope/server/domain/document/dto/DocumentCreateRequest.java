package com.coope.server.domain.document.dto;


import com.coope.server.domain.document.entity.Document;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.entity.Workspace;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DocumentCreateRequest {
    private String title;
    private Long parentId;
    private String workspaceCode;
    private String icon;

    public Document toEntity(User user, Workspace workspace, Document parentDocument) {
        return Document.builder()
                .title(this.title != null && !this.title.isBlank() ? this.title : "Untitled")
                .icon(this.icon)
                .user(user)
                .workspace(workspace)
                .parentDocument(parentDocument)
                .build();
    }
}