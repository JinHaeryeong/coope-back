package com.coope.server.domain.workspace.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.document.entity.Document;
import com.coope.server.user.domain.User;
import com.coope.server.domain.workspace.enums.WorkspaceRole;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.error.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workspaces")
@ToString(exclude = {"creator", "members", "documents"})
public class Workspace extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User creator;

    @Column(unique = true, nullable = false, length = 10)
    private String inviteCode;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @Builder
    public Workspace(String name, User creator, String inviteCode) {
        this.name = name;
        this.creator = creator;
        this.inviteCode = inviteCode;
    }

    public static Workspace createWorkspace(String name, User creator, String inviteCode) {
        Workspace workspace = Workspace.builder()
                .name(name)
                .creator(creator)
                .inviteCode(inviteCode)
                .build();

        workspace.addMember(creator, WorkspaceRole.OWNER);

        return workspace;
    }

    public void updateName(String name) {
        if (!StringUtils.hasText(name) || name.length() > 20) {
            throw new BadRequestException("워크스페이스 이름은 1~20자 사이여야 합니다.");
        }
        this.name = name;
    }

    public void addMember(User user, WorkspaceRole role) {
        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .user(user)
                .workspace(this)
                .role(role)
                .build();
        this.members.add(workspaceMember);
    }

    public void validateMember(Long userId) {
        findMember(userId);
    }

    public void validateOwner(Long userId) {
        WorkspaceMember member = findMember(userId);
        if (!member.isOwner()) {
            throw new AccessDeniedException("소유자 권한이 없습니다.");
        }
    }

    public void validateEditor(Long userId) {
        WorkspaceMember member = findMember(userId);
        if (!member.isEditor()) {
            throw new AccessDeniedException("편집 권한이 없습니다.");
        }
    }

    private WorkspaceMember findMember(Long userId) {
        return this.members.stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("해당 워크스페이스의 멤버가 아닙니다."));
    }
}