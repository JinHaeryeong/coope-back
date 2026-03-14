package com.coope.server.workspace.infrastructure;

import com.coope.server.workspace.domain.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberJpaRepository extends JpaRepository<WorkspaceMember, Long> {

    @Query("select wm from WorkspaceMember wm " +
            "join fetch wm.workspace " +
            "where wm.user.id = :userId " +
            "order by case when wm.role = 'OWNER' then 0 else 1 end, " +
            "wm.workspace.createdAt desc")
    List<WorkspaceMember> findAllByUserId(@Param("userId") Long userId);

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
