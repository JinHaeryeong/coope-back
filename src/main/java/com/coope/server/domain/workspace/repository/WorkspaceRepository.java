package com.coope.server.domain.workspace.repository;

import com.coope.server.domain.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
