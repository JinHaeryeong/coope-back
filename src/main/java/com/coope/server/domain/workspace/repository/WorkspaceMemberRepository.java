package com.coope.server.domain.workspace.repository;

import com.coope.server.domain.workspace.entity.WorkspaceMember;
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

    @org.springframework.cache.annotation.Cacheable(value = "workspaceRole", key = "#workspaceId + ':' + #userId")
    @Query("SELECT wm.role FROM WorkspaceMember wm WHERE wm.workspace.id = :workspaceId AND wm.user.id = :userId")
    Optional<String> findRoleByWorkspaceIdAndUserId(@Param("workspaceId") Long workspaceId, @Param("userId") Long userId);

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    long countByUserId(Long userId);

    List<WorkspaceMember> findAllByWorkspaceId(Long workspaceId);

    @Query("SELECT wm FROM WorkspaceMember wm " +
            "JOIN wm.workspace w " +
            "WHERE w.inviteCode = :inviteCode AND wm.user.id = :userId")
    Optional<WorkspaceMember> findByWorkspaceInviteCodeAndUserId(@Param("inviteCode") String inviteCode,
                                                                 @Param("userId") Long userId);
}
