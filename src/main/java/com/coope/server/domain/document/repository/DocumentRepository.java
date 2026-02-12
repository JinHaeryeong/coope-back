package com.coope.server.domain.document.repository;

import com.coope.server.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("select d from Document d " +
            "join fetch d.user " +
            "where d.workspace.id = :workspaceId " +
            "and d.parentDocument is null " +
            "and d.isArchived = false " +
            "order by d.createdAt desc")
    List<Document> findAllRootDocuments(@Param("workspaceId") Long workspaceId);

    @Query("select d from Document d " +
            "join fetch d.user " +
            "where d.parentDocument.id = :parentId " +
            "and d.isArchived = false " +
            "order by d.createdAt desc")
    List<Document> findAllByParentDocumentIdWithUser(@Param("parentId") Long parentId);

    @Query("SELECT d, " +
            "(SELECT COUNT(c) > 0 FROM Document c WHERE c.parentDocument = d AND c.isArchived = false) " +
            "FROM Document d JOIN FETCH d.user " +
            "WHERE d.workspace.id = :workspaceId " +
            "AND ( " +
            "    (:parentId IS NULL AND d.parentDocument IS NULL) " +
            "    OR " +
            "    (:parentId IS NOT NULL AND d.parentDocument.id = :parentId) " +
            ") " +
            "AND d.isArchived = false " +
            "ORDER BY d.createdAt DESC")
    List<Object[]> findAllByWorkspaceAndParentWithChildCheck(
            @Param("workspaceId") Long workspaceId,
            @Param("parentId") Long parentId
    );

    @Query("select d from Document d " +
            "join fetch d.user " +
            "where d.workspace.id = :workspaceId " +
            "and d.isArchived = true " +
            "order by d.updatedAt desc")
    List<Document> findAllTrashDocuments(@Param("workspaceId") Long workspaceId);

    boolean existsByParentDocumentAndArchivedFalse(Document parentDocument);
}