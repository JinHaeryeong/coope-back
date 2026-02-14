package com.coope.server.domain.workspace.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.document.entity.Document;
import com.coope.server.domain.user.entity.User;
import com.coope.server.global.error.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.*;

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

    public void updateName(String name) {
        if (name == null || name.isBlank() || name.length() > 20) {
            throw new BadRequestException("워크스페이스 이름은 1~20자 사이여야 합니다.");
        }
        this.name = name;
    }
}