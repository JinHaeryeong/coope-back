package com.coope.server.domain.document.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_workspace_id", columnList = "workspace_id"),
        @Index(name = "idx_parent_id", columnList = "parent_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String icon;
    private String coverImage;

    @Column(nullable = false)
    private boolean isArchived;

    @Column(nullable = false)
    private boolean isPublished;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Document parentDocument;

    @OneToMany(mappedBy = "parentDocument", cascade = CascadeType.ALL)
    private List<Document> childDocuments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Document(String title, String content, String icon, String coverImage,
                    Workspace workspace, User user, Document parentDocument) {
        this.title = title;
        this.content = content;
        this.icon = icon;
        this.coverImage = coverImage;
        this.workspace = workspace;
        this.user = user;
        this.parentDocument = parentDocument;
        this.isArchived = false;
        this.isPublished = false;
    }

    public void restore() {
        this.isArchived = false;

        // 노션 로직 반영
        // 부모가 없거나, 부모가 이미 휴지통(archived) 상태라면 최상위(null)로 이동
        if (this.parentDocument != null && this.parentDocument.isArchived()) {
            this.parentDocument = null;
        }
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void archiveWithChildren() {
        this.isArchived = true;
        if (this.childDocuments != null) {
            this.childDocuments.forEach(Document::archiveWithChildren);
        }
    }
}