package com.coope.server.domain.workspace.repository;

import com.coope.server.domain.workspace.entity.WorkspaceMember;
import com.coope.server.domain.workspace.enums.WorkspaceRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    @Query("select wm from WorkspaceMember wm " +
            "join fetch wm.workspace " +
            "where wm.user.id = :userId " +
            "order by case when wm.role = 'OWNER' then 0 else 1 end, " +
            "wm.workspace.createdAt desc")
    List<WorkspaceMember> findAllByUserId(@Param("userId") Long userId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    long countByUserId(Long userId);

    boolean existsByWorkspaceIdAndUserIdAndRole(Long workspaceId, Long userId, WorkspaceRole role);

    List<WorkspaceMember> findAllByWorkspaceId(Long workspaceId);
}
