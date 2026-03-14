package com.coope.server.document.infrastructure;

import com.coope.server.document.domain.Document;
import com.coope.server.document.domain.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DocumentRepositoryImpl implements DocumentRepository {

    private final DocumentJpaRepository documentJpaRepository;

    @Override
    public Optional<Document> findById(Long id) {
        return documentJpaRepository.findById(id);
    }

    @Override
    public List<Object[]> findAllByWorkspaceAndParentWithChildCheck(Long workspaceId, Long parentId) {
        return documentJpaRepository.findAllByWorkspaceAndParentWithChildCheck(workspaceId, parentId);
    }

    @Override
    public List<Document> findAllTrashDocuments(Long workspaceId) {
        return documentJpaRepository.findAllTrashDocuments(workspaceId);
    }

    @Override
    public boolean existsByParentDocumentAndArchivedFalse(Document parentDocument) {
        return documentJpaRepository.existsByParentDocumentAndArchivedFalse(parentDocument);
    }

    @Override
    public void updateOnlyContent(Long id, String content) {
        documentJpaRepository.updateOnlyContent(id, content);
    }

    @Override
    public Optional<Document> findByIdWithWorkspace(Long id) {
        return documentJpaRepository.findByIdWithWorkspace(id);
    }

    @Override
    public Document save(Document document) {
        return documentJpaRepository.save(document);
    }

    @Override
    public void delete(Document document) {
        documentJpaRepository.delete(document);
    }
}
