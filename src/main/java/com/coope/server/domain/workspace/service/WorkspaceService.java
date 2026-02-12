package com.coope.server.domain.workspace.service;

import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.dto.WorkspaceResponse;
import com.coope.server.domain.workspace.dto.WorkspaceWriteRequest;
import com.coope.server.domain.workspace.entity.Workspace;
import com.coope.server.domain.workspace.entity.WorkspaceMember;
import com.coope.server.domain.workspace.repository.WorkspaceMemberRepository;
import com.coope.server.domain.workspace.repository.WorkspaceRepository;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.error.exception.WorkspaceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public Workspace getByInviteCode(String inviteCode) {
        return workspaceRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다. InviteCode: " + inviteCode));
    }

    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceWriteRequest request, User user) {
        String inviteCode = generateUniqueInviteCode();

        Workspace workspace = workspaceRepository.save(request.toEntity(user, inviteCode));

        WorkspaceMember member = workspaceMemberRepository.save(WorkspaceMember.createOwner(user, workspace));

        return WorkspaceResponse.from(workspace, member.getRole());
    }

    private String generateUniqueInviteCode() {
        String inviteCode;
        do {
            inviteCode = UUID.randomUUID().toString().substring(0, 8);
        } while (workspaceRepository.existsByInviteCode(inviteCode)); // 중복되면 다시 생성

        return inviteCode;
    }


    public List<WorkspaceResponse> getMyWorkspaces(Long userId) {
        // WorkspaceMember 테이블을 통해 내가 속한 워크스페이스들을 가져옴
        return workspaceMemberRepository.findAllByUserId(userId).stream()
                .map(member -> WorkspaceResponse.from(member.getWorkspace(), member.getRole()))
                .collect(Collectors.toList());
    }

    // 사용자가 워크스페이스 멤버인지 검증
    public void validateMember(Long workspaceId, Long userId) {
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);

        if (!isMember) {
            throw new AccessDeniedException("해당 워크스페이스에 대한 접근 권한이 없습니다.");
        }
    }
}