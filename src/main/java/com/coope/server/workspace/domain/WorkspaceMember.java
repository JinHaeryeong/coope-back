package com.coope.server.workspace.domain;

import com.coope.server.user.domain.User;
import com.coope.server.workspace.domain.enums.WorkspaceRole;
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

    public void updateRole(WorkspaceRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("변경할 권한이 유효하지 않습니다.");
        }
        this.role = newRole;
    }

    public boolean isOwner() {
        return this.role == WorkspaceRole.OWNER;
    }

    public boolean isEditor() {
        return this.role == WorkspaceRole.OWNER || this.role == WorkspaceRole.EDITOR;
    }

    public static WorkspaceMember createMember(User user, Workspace workspace, WorkspaceRole role) {
        return WorkspaceMember.builder()
                .user(user)
                .workspace(workspace)
                .role(role)
                .build();
    }

    public static WorkspaceMember createOwner(User user, Workspace workspace) {
        return WorkspaceMember.builder()
                .user(user)
                .workspace(workspace)
                .role(WorkspaceRole.OWNER)
                .build();
    }
}
