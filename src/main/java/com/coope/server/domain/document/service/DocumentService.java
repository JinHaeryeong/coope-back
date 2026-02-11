package com.coope.server.domain.document.service;

import com.coope.server.domain.document.dto.DocumentCreateRequest;
import com.coope.server.domain.document.dto.DocumentResponse;
import com.coope.server.domain.document.entity.Document;
import com.coope.server.domain.document.repository.DocumentRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.entity.Workspace;
import com.coope.server.domain.workspace.service.WorkspaceService;
import com.coope.server.global.error.exception.DocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final WorkspaceService workspaceService;

    public List<DocumentResponse> getSidebarDocuments(String workspaceCode, Long parentId) {
        Workspace workspace = workspaceService.getByInviteCode(workspaceCode);

        List<Object[]> results = documentRepository.findAllByWorkspaceAndParentWithChildCheck(workspace.getId(), parentId);

        return results.stream()
                .map(result -> {
                    Document document = (Document) result[0];
                    Boolean hasChildren = (Boolean) result[1];
                    return DocumentResponse.from(document, hasChildren);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request, User user) {
        Workspace workspace = workspaceService.getByInviteCode(request.getWorkspaceCode());


        Document parentDocument = null;
        if (request.getParentId() != null) {
            parentDocument = documentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new DocumentNotFoundException("부모 문서를 찾을 수 없습니다. ID: " + request.getParentId()));
        }

        Document document = request.toEntity(user, workspace, parentDocument);
        Document savedDocument = documentRepository.save(document);

        return DocumentResponse.from(savedDocument, false);
    }
}