package com.coope.server.domain.comment.service;

import com.coope.server.domain.comment.dto.CommentRequest;
import com.coope.server.domain.comment.dto.CommentResponse;
import com.coope.server.domain.comment.entity.Comment;
import com.coope.server.domain.comment.repository.CommentRepository;
import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.notice.repository.NoticeRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.enums.Role;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.error.exception.CommentNotFoundException;
import com.coope.server.global.error.exception.NoticeNotFoundException;
import com.coope.server.global.infra.file.FileDeleteEvent;
import com.coope.server.global.infra.file.FileService;
import com.coope.server.global.infra.file.ImageCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final NoticeRepository noticeRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentResponse createComment(Long noticeId, CommentRequest request, User user) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항이 존재하지 않습니다."));

        String savedImageUrl = fileService.upload(request.getFile(), ImageCategory.COMMENT);

        Comment comment = Comment.createComment(notice, user, request.getContent(), savedImageUrl);
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    public List<CommentResponse> getComments(Long noticeId) {
        return commentRepository.findAllByNoticeIdWithUser(noticeId)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long noticeId, Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글이 존재하지 않습니다."));

        validateCommentOwnership(noticeId, comment);

        if (!comment.getUser().getId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        String currentImageUrl = comment.getImageUrl();
        commentRepository.delete(comment);

        if (currentImageUrl != null) {
            eventPublisher.publishEvent(new FileDeleteEvent(currentImageUrl, ImageCategory.COMMENT));
        }
    }

    @Transactional
    public CommentResponse updateComment(Long noticeId, Long commentId, CommentRequest requestDto, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글이 존재하지 않습니다."));

        validateCommentOwnership(noticeId, comment);

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        handleImageUpdate(comment, requestDto);

        comment.update(requestDto.getContent());
        return CommentResponse.from(comment);
    }

    private void validateCommentOwnership(Long noticeId, Comment comment) {
        if (!comment.getNotice().getId().equals(noticeId)) {
            throw new AccessDeniedException("해당 공지사항의 댓글이 아닙니다.");
        }
    }

    private void handleImageUpdate(Comment comment, CommentRequest requestDto) {
        String currentImageUrl = comment.getImageUrl();
        if (Boolean.TRUE.equals(requestDto.getDeleteImage()) || (requestDto.getFile() != null && !requestDto.getFile().isEmpty())) {
            if (currentImageUrl != null) {
                eventPublisher.publishEvent(new FileDeleteEvent(currentImageUrl, ImageCategory.COMMENT));
                comment.updateImage(null);
            }
        }
        if (requestDto.getFile() != null && !requestDto.getFile().isEmpty()) {
            String newImageUrl = fileService.upload(requestDto.getFile(), ImageCategory.COMMENT);
            comment.updateImage(newImageUrl);
        }
    }
}