package com.coope.server.domain.workspace.entity;

import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.enums.WorkspaceRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workspace_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "workspace_id"}))
@ToString(exclude = {"workspace", "user"})
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    @Builder
    public WorkspaceMember(User user, Workspace workspace, WorkspaceRole role) {
        this.user = user;
        this.workspace = workspace;
        this.role = role;
    }

    public boolean isOwner() {
        return this.role == WorkspaceRole.OWNER;
    }

    public boolean isEditor() {
        return this.role == WorkspaceRole.OWNER || this.role == WorkspaceRole.EDITOR;
    }

    public static WorkspaceMember createOwner(User user, Workspace workspace) {
        return WorkspaceMember.builder()
                .user(user)
                .workspace(workspace)
                .role(WorkspaceRole.OWNER)
                .build();
    }
}