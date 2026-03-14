package com.coope.server.document.infrastructure;

import com.coope.server.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentJpaRepository extends JpaRepository<Document, Long> {

    @Query("SELECT d, " +
            "(SELECT COUNT(c) > 0 FROM Document c WHERE c.parentDocument = d AND c.archived = false) " +
            "FROM Document d JOIN FETCH d.user " +
            "WHERE d.workspace.id = :workspaceId " +
            "AND ( " +
            "    (:parentId IS NULL AND d.parentDocument IS NULL) " +
            "    OR " +
            "    (:parentId IS NOT NULL AND d.parentDocument.id = :parentId) " +
            ") " +
            "AND d.archived = false " +
            "ORDER BY d.createdAt DESC")
    List<Object[]> findAllByWorkspaceAndParentWithChildCheck(
            @Param("workspaceId") Long workspaceId,
            @Param("parentId") Long parentId
    );

    @Query("select d from Document d " +
            "join fetch d.user " +
            "where d.workspace.id = :workspaceId " +
            "and d.archived = true " +
            "order by d.updatedAt desc")
    List<Document> findAllTrashDocuments(@Param("workspaceId") Long workspaceId);

    @Query("SELECT COUNT(d) > 0 FROM Document d WHERE d.parentDocument = :parentDocument AND d.archived = false")
    boolean existsByParentDocumentAndArchivedFalse(@Param("parentDocument") Document parentDocument);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Document d SET d.content = :content, d.updatedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void updateOnlyContent(@Param("id") Long id, @Param("content") String content);

    @Query("SELECT d FROM Document d JOIN FETCH d.workspace WHERE d.id = :id")
    Optional<Document> findByIdWithWorkspace(@Param("id") Long id);
}
