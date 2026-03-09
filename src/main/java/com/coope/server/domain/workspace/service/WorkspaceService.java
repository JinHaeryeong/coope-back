package com.coope.server.domain.workspace.service;

import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.dto.WorkspaceMemberResponse;
import com.coope.server.domain.workspace.dto.WorkspaceResponse;
import com.coope.server.domain.workspace.dto.WorkspaceWriteRequest;
import com.coope.server.domain.workspace.entity.Workspace;
import com.coope.server.domain.workspace.entity.WorkspaceMember;
import com.coope.server.domain.workspace.enums.WorkspaceRole;
import com.coope.server.domain.workspace.repository.WorkspaceMemberRepository;
import com.coope.server.domain.workspace.repository.WorkspaceRepository;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.error.exception.BadRequestException;
import com.coope.server.global.error.exception.WorkspaceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    public Workspace getByInviteCode(String inviteCode) {
        return workspaceRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다. InviteCode: " + inviteCode));
    }

    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceWriteRequest request, User user) {
        String inviteCode = generateUniqueInviteCode();

        Workspace workspace = workspaceRepository.save(request.toEntity(user, inviteCode));
        WorkspaceMember member = workspaceMemberRepository.save(WorkspaceMember.createOwner(user, workspace));

        notifyUserWorkspaceUpdate(user.getId());

        return WorkspaceResponse.from(workspace, member.getRole());
    }

    private String generateUniqueInviteCode() {
        String inviteCode;
        do {
            inviteCode = UUID.randomUUID().toString().substring(0, 8);
        } while (workspaceRepository.existsByInviteCode(inviteCode)); // 중복되면 다시 생성

        return inviteCode;
    }


    @Transactional
    @CacheEvict(value = "workspaceMember", key = "#result.id + ':' + #user.id")
    public WorkspaceResponse joinWorkspace(String inviteCode, User user) {
        Workspace workspace = workspaceRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new WorkspaceNotFoundException("초대 코드가 잘못되었습니다."));

        boolean isAlreadyMember = workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                workspace.getId(), user.getId());

        if (isAlreadyMember) {
            return WorkspaceResponse.from(workspace, WorkspaceRole.VIEWER, "already_member");
        }

        WorkspaceMember newMember = WorkspaceMember.createMember(user, workspace, WorkspaceRole.VIEWER);
        workspaceMemberRepository.save(newMember);

        notifyUserWorkspaceUpdate(user.getId());

        return WorkspaceResponse.from(workspace, WorkspaceRole.VIEWER, "joined");
    }

    public WorkspaceResponse getWorkspaceByCode(String workspaceCode, User user) {
        Workspace workspace = getByInviteCode(workspaceCode);

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspace.getId(), user.getId())
                .orElseThrow(() -> new AccessDeniedException("해당 워크스페이스의 멤버가 아닙니다."));

        return WorkspaceResponse.from(workspace, member.getRole());
    }

    @Transactional
    public WorkspaceResponse updateWorkspaceName(String workspaceCode, String newName, User user) {
        Workspace workspace = getByInviteCode(workspaceCode);

        // OWNER만 수정 가능하도록 설정
        validateOwner(workspace.getId(), user.getId());

        workspace.updateName(newName);

        notifyAllMembers(workspace.getId());

        // 현재 유저의 역할을 함께 반환
        return WorkspaceResponse.from(workspace, WorkspaceRole.OWNER);
    }

    public List<WorkspaceMemberResponse> getWorkspaceMembers(String workspaceCode, Long userId) {
        Workspace workspace = getByInviteCode(workspaceCode);

        validateMember(workspace.getId(), userId);

        return workspaceMemberRepository.findAllByWorkspaceId(workspace.getId()).stream()
                .map(member -> WorkspaceMemberResponse.builder()
                        .userId(member.getUser().getId())
                        .nickname(member.getUser().getNickname())
                        .role(member.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "workspaceRole", key = "#workspaceId + ':' + #targetUserId") // 바뀐 사람의 캐시를 즉시 삭제!
    public void updateMemberRole(Long workspaceId, Long targetUserId, WorkspaceRole newRole, User user) {
        validateOwner(workspaceId, user.getId());

        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new AccessDeniedException("해당 멤버를 찾을 수 없습니다."));

        if (targetUserId.equals(user.getId())) {
            throw new BadRequestException("소유자 본인의 권한은 변경할 수 없습니다.");
        }

        targetMember.updateRole(newRole);

        notifyUserWorkspaceUpdate(targetUserId);
    }

    @Transactional
    @CacheEvict(value = "workspaceMember", allEntries = true)
    public void deleteWorkspace(String workspaceCode, User user) {
        Workspace workspace = getByInviteCode(workspaceCode);

        validateOwner(workspace.getId(), user.getId());
        long membershipCount = workspaceMemberRepository.countByUserId(user.getId());
        if (membershipCount <= 1) {
            throw new BadRequestException("최소 한 개의 워크스페이스는 유지해야 합니다.");
        }

        notifyAllMembers(workspace.getId());

        workspaceRepository.delete(workspace);
    }

    public List<WorkspaceResponse> getMyWorkspaces(Long userId) {
        return workspaceMemberRepository.findAllByUserId(userId).stream()
                .map(member -> WorkspaceResponse.from(member.getWorkspace(), member.getRole()))
                .collect(Collectors.toList());
    }

    public void validateMember(Long workspaceId, Long userId) {
        Object roleObj = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 워크스페이스의 멤버가 아닙니다."));

        WorkspaceRole role = convertToRole(roleObj);
    }

    public void validateEditor(Long workspaceId, Long userId) {
        Object roleObj = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 워크스페이스의 멤버가 아닙니다."));

        WorkspaceRole role = convertToRole(roleObj);

        if (role == WorkspaceRole.VIEWER) {
            throw new AccessDeniedException("편집 권한이 없습니다.");
        }
    }

    public void validateOwner(Long workspaceId, Long userId) {
        Object roleObj = workspaceMemberRepository.findRoleByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new AccessDeniedException("해당 워크스페이스의 멤버가 아닙니다."));

        WorkspaceRole role = convertToRole(roleObj);

        if (role != WorkspaceRole.OWNER) {
            throw new AccessDeniedException("소유자 권한이 없습니다.");
        }
    }

    private void notifyAllMembers(Long workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepository.findAllByWorkspaceId(workspaceId);

        for (WorkspaceMember member : members) {
            notifyUserWorkspaceUpdate(member.getUser().getId());
        }
    }

    private void notifyUserWorkspaceUpdate(Long userId) {
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/workspace", "REFRESH");
    }

    private WorkspaceRole convertToRole(Object roleObj) {
        if (roleObj instanceof WorkspaceRole) {
            return (WorkspaceRole) roleObj;
        }
        return WorkspaceRole.valueOf(roleObj.toString());
    }
}