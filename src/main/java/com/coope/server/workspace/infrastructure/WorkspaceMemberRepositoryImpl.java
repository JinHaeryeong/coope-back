package com.coope.server.workspace.infrastructure;

import com.coope.server.workspace.domain.WorkspaceMember;
import com.coope.server.workspace.domain.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkspaceMemberRepositoryImpl implements WorkspaceMemberRepository {

    private final WorkspaceMemberJpaRepository workspaceMemberJpaRepository;

    @Override
    public List<WorkspaceMember> findAllByUserId(Long userId) {
        return workspaceMemberJpaRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId) {
        return workspaceMemberJpaRepository.findByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Override
    public boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId) {
        return workspaceMemberJpaRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Override
    public long countByUserId(Long userId) {
        return workspaceMemberJpaRepository.countByUserId(userId);
    }

    @Override
    public List<WorkspaceMember> findAllByWorkspaceId(Long workspaceId) {
        return workspaceMemberJpaRepository.findAllByWorkspaceId(workspaceId);
    }

    @Override
    public Optional<WorkspaceMember> findByWorkspaceInviteCodeAndUserId(String inviteCode, Long userId) {
        return workspaceMemberJpaRepository.findByWorkspaceInviteCodeAndUserId(inviteCode, userId);
    }
}
