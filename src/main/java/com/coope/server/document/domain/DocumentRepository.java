package com.coope.server.document.domain;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository {
    Optional<Document> findById(Long id);
    List<Object[]> findAllByWorkspaceAndParentWithChildCheck(Long workspaceId, Long parentId);
    List<Document> findAllTrashDocuments(Long workspaceId);
    boolean existsByParentDocumentAndArchivedFalse(Document parentDocument);
    void updateOnlyContent(Long id, String content);
    Optional<Document> findByIdWithWorkspace(Long id);
    Document save(Document document);
    void delete(Document document);
}
