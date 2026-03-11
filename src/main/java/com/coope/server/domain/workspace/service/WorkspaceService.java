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
    private final WorkspaceRoleService workspaceRoleService;

    public Workspace getByInviteCode(String inviteCode) {
        return workspaceRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new WorkspaceNotFoundException("워크스페이스를 찾을 수 없습니다."));
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
    public WorkspaceResponse createWorkspace(WorkspaceWriteRequest request, User user) {
        String inviteCode = generateUniqueInviteCode();

        Workspace workspace = Workspace.createWorkspace(request.getName(), user, inviteCode);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        notifyUserWorkspaceUpdate(user.getId());
        return WorkspaceResponse.from(savedWorkspace, WorkspaceRole.OWNER);
    }

    @Transactional
    @CacheEvict(value = "workspaceRole", key = "#workspaceId + ':' + #user.id")
    public WorkspaceResponse joinWorkspace(String inviteCode, User user) {
        Workspace workspace = getByInviteCode(inviteCode);

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), user.getId())) {
            return WorkspaceResponse.from(workspace, WorkspaceRole.VIEWER, "already_member");
        }

        workspace.addMember(user, WorkspaceRole.VIEWER);
        workspaceRepository.save(workspace);

        notifyUserWorkspaceUpdate(user.getId());
        return WorkspaceResponse.from(workspace, WorkspaceRole.VIEWER, "joined");
    }

    @Transactional
    public WorkspaceResponse updateWorkspaceName(String workspaceCode, String newName, User user) {
        Workspace workspace = getByInviteCode(workspaceCode);
        validateOwner(workspace.getId(), user.getId());

        workspace.updateName(newName);

        notifyAllMembers(workspace.getId());
        return WorkspaceResponse.from(workspace, WorkspaceRole.OWNER);
    }

    @Transactional
    public void updateMemberRoleByCode(String workspaceCode, Long targetUserId, WorkspaceRole newRole, User user) {
        Workspace workspace = getByInviteCode(workspaceCode);
        updateMemberRole(workspace.getId(), targetUserId, newRole, user);
    }

    @Transactional
    @CacheEvict(value = "workspaceRole", key = "#workspaceId + ':' + #targetUserId")
    public void updateMemberRole(Long workspaceId, Long targetUserId, WorkspaceRole newRole, User user) {
        if (targetUserId.equals(user.getId())) {
            throw new BadRequestException("소유자 본인의 권한은 변경할 수 없습니다.");
        }

        validateOwner(workspaceId, user.getId());
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new AccessDeniedException("해당 멤버를 찾을 수 없습니다."));

        targetMember.updateRole(newRole);

        notifyUserWorkspaceUpdate(targetUserId);
    }

    @Transactional
    @CacheEvict(value = "workspaceRole", allEntries = true)
    public void deleteWorkspace(String workspaceCode, User user) {
        Workspace workspace = getByInviteCode(workspaceCode);
        validateOwner(workspace.getId(), user.getId());

        if (workspaceMemberRepository.countByUserId(user.getId()) <= 1) {
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

    public WorkspaceResponse getWorkspaceByCode(String workspaceCode, User user) {
        Workspace workspace = getByInviteCode(workspaceCode);
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), user.getId())
                .orElseThrow(() -> new AccessDeniedException("멤버가 아닙니다."));
        return WorkspaceResponse.from(workspace, member.getRole());
    }

    public void validateMember(Long workspaceId, Long userId) {
        workspaceRoleService.getUserRole(workspaceId, userId);
    }

    public void validateEditor(Long workspaceId, Long userId) {
        WorkspaceRole role = WorkspaceRole.valueOf(workspaceRoleService.getUserRole(workspaceId, userId));
        if (role == WorkspaceRole.VIEWER) {
            throw new AccessDeniedException("편집 권한이 없습니다.");
        }
    }

    public void validateOwner(Long workspaceId, Long userId) {
        WorkspaceRole role = WorkspaceRole.valueOf(workspaceRoleService.getUserRole(workspaceId, userId));
        if (role != WorkspaceRole.OWNER) {
            throw new AccessDeniedException("소유자 권한이 없습니다.");
        }
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8);
        } while (workspaceRepository.existsByInviteCode(code));
        return code;
    }

    private void notifyAllMembers(Long workspaceId) {
        workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
                .forEach(member -> notifyUserWorkspaceUpdate(member.getUser().getId()));
    }

    private void notifyUserWorkspaceUpdate(Long userId) {
        messagingTemplate.convertAndSend("/topic/user/" + userId + "/workspace", "REFRESH");
    }
}