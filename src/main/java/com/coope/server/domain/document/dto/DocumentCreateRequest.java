package com.coope.server.domain.document.dto;


import com.coope.server.domain.document.entity.Document;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.entity.Workspace;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DocumentCreateRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private Long parentId;

    @NotBlank(message = "워크스페이스 코드는 필수입니다.")
    private String workspaceCode;
    private String icon;

    public Document toEntity(User user, Workspace workspace, Document parentDocument) {
        return Document.builder()
                .title(this.title)
                .icon(this.icon)
                .user(user)
                .workspace(workspace)
                .parentDocument(parentDocument)
                .build();
    }
}