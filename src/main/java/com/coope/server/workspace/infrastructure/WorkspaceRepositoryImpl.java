package com.coope.server.workspace.infrastructure;

import com.coope.server.workspace.domain.Workspace;
import com.coope.server.workspace.domain.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkspaceRepositoryImpl implements WorkspaceRepository {

    private final WorkspaceJpaRepository workspaceJpaRepository;

    @Override
    public Optional<Workspace> findById(Long id) {
        return workspaceJpaRepository.findById(id);
    }

    @Override
    public Optional<Workspace> findByInviteCode(String inviteCode) {
        return workspaceJpaRepository.findByInviteCode(inviteCode);
    }

    @Override
    public boolean existsByInviteCode(String inviteCode) {
        return workspaceJpaRepository.existsByInviteCode(inviteCode);
    }

    @Override
    public Workspace save(Workspace workspace) {
        return workspaceJpaRepository.save(workspace);
    }

    @Override
    public void delete(Workspace workspace) {
        workspaceJpaRepository.delete(workspace);
    }
}
