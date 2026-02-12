package com.coope.server.domain.workspace.entity;

import com.coope.server.domain.common.entity.BaseTimeEntity;
import com.coope.server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "workspaces")
@ToString(exclude = {"user"})
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

    @Builder
    public Workspace(String name, User creator, String inviteCode) {
        this.name = name;
        this.creator = creator;
        this.inviteCode = inviteCode;
    }
}