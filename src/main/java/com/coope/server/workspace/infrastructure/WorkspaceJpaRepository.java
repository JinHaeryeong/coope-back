package com.coope.server.workspace.infrastructure;

import com.coope.server.workspace.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceJpaRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
}
