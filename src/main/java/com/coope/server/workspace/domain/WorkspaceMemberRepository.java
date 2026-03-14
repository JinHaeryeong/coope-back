package com.coope.server.workspace.domain;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository {
    List<WorkspaceMember> findAllByUserId(Long userId);
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);
    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);
    long countByUserId(Long userId);
    List<WorkspaceMember> findAllByWorkspaceId(Long workspaceId);
    Optional<WorkspaceMember> findByWorkspaceInviteCodeAndUserId(String inviteCode, Long userId);
}
