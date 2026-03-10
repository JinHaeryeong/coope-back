package com.coope.server.domain.workspace.service;

import com.coope.server.domain.workspace.entity.WorkspaceMember;
import com.coope.server.domain.workspace.repository.WorkspaceMemberRepository;
import com.coope.server.global.error.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceRoleService {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Cacheable(value = "workspaceRole", key = "#workspaceId + ':' + #userId")
    public String getUserRole(Long workspaceId, Long userId) {

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 워크스페이스의 멤버가 아닙니다."));

        return member.getRole().name();
    }
}