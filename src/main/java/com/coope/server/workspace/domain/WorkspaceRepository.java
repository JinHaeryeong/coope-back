package com.coope.server.workspace.domain;

import java.util.Optional;

public interface WorkspaceRepository {
    Optional<Workspace> findById(Long id);
    Optional<Workspace> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
    Workspace save(Workspace workspace);
    void delete(Workspace workspace);
}
