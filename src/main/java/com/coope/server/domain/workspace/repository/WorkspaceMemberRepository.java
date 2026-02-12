package com.coope.server.domain.workspace.repository;

import com.coope.server.domain.workspace.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {
    /**
     * 유저 ID를 기반으로 가입된 모든 워크스페이스 멤버 정보를 조회
     * fetch join을 사용하여 연관된 Workspace 엔티티까지 한 번에 가져옴
     */
    @Query("select wm from WorkspaceMember wm " +
            "join fetch wm.workspace " +
            "where wm.user.id = :userId")
    List<WorkspaceMember> findAllByUserId(@Param("userId") Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);
}
