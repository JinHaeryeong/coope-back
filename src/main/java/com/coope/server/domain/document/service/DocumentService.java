package com.coope.server.domain.document.service;

import com.coope.server.domain.document.dto.DocumentCreateRequest;
import com.coope.server.domain.document.dto.DocumentEvent;
import com.coope.server.domain.document.dto.DocumentResponse;
import com.coope.server.domain.document.dto.DocumentUpdateRequest;
import com.coope.server.domain.document.entity.Document;
import com.coope.server.domain.document.repository.DocumentRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.entity.Workspace;
import com.coope.server.domain.workspace.service.WorkspaceService;
import com.coope.server.global.error.exception.DocumentNotFoundException;
import com.coope.server.global.infra.FileService;
import com.coope.server.global.infra.ImageCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final FileService fileService;
    private final SimpMessagingTemplate messagingTemplate;

    public List<DocumentResponse> getSidebarDocuments(String workspaceCode, Long parentId, User user) {
        Workspace workspace = workspaceService.getByInviteCode(workspaceCode);

        workspaceService.validateMember(workspace.getId(), user.getId());

        List<Object[]> results = documentRepository.findAllByWorkspaceAndParentWithChildCheck(workspace.getId(), parentId);

        return results.stream()
                .map(result -> DocumentResponse.of((Document) result[0], (Boolean) result[1]))
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponse createDocument(DocumentCreateRequest request, User user) {
        Workspace workspace = workspaceService.getByInviteCode(request.getWorkspaceCode());

        workspaceService.validateMember(workspace.getId(), user.getId());

        Document parentDocument = null;
        if (request.getParentId() != null) {
            parentDocument = documentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new DocumentNotFoundException("부모 문서를 찾을 수 없습니다. ID: " + request.getParentId()));

            if (!parentDocument.getWorkspace().getId().equals(workspace.getId())) {
                throw new DocumentNotFoundException("부모 문서가 현재 워크스페이스에 존재하지 않습니다.");
            }
        }

        Document document = request.toEntity(user, workspace, parentDocument);
        Document savedDocument = documentRepository.save(document);

        DocumentResponse response = DocumentResponse.of(savedDocument, false);

        broadcast(request.getWorkspaceCode(), "UPSERT", response);

        return response;
    }


    @Transactional
    public DocumentResponse update(Long documentId, DocumentUpdateRequest request, User user) {
        Document document = findDocumentById(documentId);

        workspaceService.validateMember(document.getWorkspace().getId(), user.getId());

        if (request.getTitle() != null) document.updateTitle(request.getTitle());
        if (request.getContent() != null) document.updateContent(request.getContent());
        if (request.getIcon() != null) document.updateIcon(request.getIcon());

        if (request.getCoverImage() != null) {
            String oldUrl = document.getCoverImage();
            if (oldUrl != null && !oldUrl.equals(request.getCoverImage())) {
                fileService.deleteFile(oldUrl, ImageCategory.COVER);
            }
            document.updateCoverImage(request.getCoverImage());
        }

        boolean hasChildren = documentRepository.existsByParentDocumentAndArchivedFalse(document);

        DocumentResponse response = DocumentResponse.of(document, hasChildren);

        broadcast(document.getWorkspace().getInviteCode(), "UPSERT", response);

        return response;
    }

    @Transactional
    public void archiveDocument(Long documentId, User user) {
        Document document = findDocumentById(documentId);

        workspaceService.validateMember(document.getWorkspace().getId(), user.getId());

        document.archiveWithChildren();

        broadcast(document.getWorkspace().getInviteCode(), "ARCHIVE", documentId);
    }

    @Transactional
    public DocumentResponse restoreDocument(Long documentId, User user) {
        Document document = findDocumentById(documentId);

        workspaceService.validateMember(document.getWorkspace().getId(), user.getId());

        document.restore();
        DocumentResponse response = DocumentResponse.of(document, false);

        broadcast(document.getWorkspace().getInviteCode(), "UPSERT", response);

        return response;
    }

    @Transactional
    public void hardDeleteDocument(Long documentId, User user) {
        Document document = findDocumentById(documentId);
        String inviteCode = document.getWorkspace().getInviteCode();

        workspaceService.validateMember(document.getWorkspace().getId(), user.getId());

        documentRepository.delete(document);

        broadcast(inviteCode, "DELETE", documentId);
    }

    public List<DocumentResponse> getTrashDocuments(String workspaceCode, User user) {
        Workspace workspace = workspaceService.getByInviteCode(workspaceCode);

        workspaceService.validateMember(workspace.getId(), user.getId());

        return documentRepository.findAllTrashDocuments(workspace.getId())
                .stream()
                .map(doc -> DocumentResponse.of(doc, false))
                .collect(Collectors.toList());
    }


    public DocumentResponse getDocumentDetail(Long documentId, String workspaceCode, User user) {
        Document document = findDocumentById(documentId);

        if (!document.getWorkspace().getInviteCode().equals(workspaceCode)) {
            throw new DocumentNotFoundException("해당 워크스페이스에 존재하지 않는 문서입니다.");
        }

        workspaceService.validateMember(document.getWorkspace().getId(), user.getId());

        boolean hasChildren = documentRepository.existsByParentDocumentAndArchivedFalse(document);

        return DocumentResponse.of(document, hasChildren);
    }

    private Document findDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("문서를 찾을 수 없습니다. ID: " + documentId));
    }

    private void broadcast(String workspaceCode, String type, Object data) {
        DocumentEvent event = DocumentEvent.builder()
                .type(type)
                .data(data)
                .build();

        messagingTemplate.convertAndSend("/topic/workspace/" + workspaceCode, event);
    }
}