package com.coope.server.domain.comment.service;

import com.coope.server.domain.comment.dto.CommentRequest;
import com.coope.server.domain.comment.dto.CommentResponse;
import com.coope.server.domain.comment.entity.Comment;
import com.coope.server.domain.comment.repository.CommentRepository;
import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.notice.repository.NoticeRepository;
import com.coope.server.domain.user.entity.User;
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
        Comment comment = findCommentOrThrow(commentId);

        comment.validateNoticeOwnership(noticeId);
        comment.validateDeletionAuthority(user);

        String currentImageUrl = comment.getImageUrl();
        commentRepository.delete(comment);

        if (currentImageUrl != null) {
            eventPublisher.publishEvent(new FileDeleteEvent(currentImageUrl, ImageCategory.COMMENT));
        }
    }

    @Transactional
    public CommentResponse updateComment(Long noticeId, Long commentId, CommentRequest request, User user) {
        Comment comment = findCommentOrThrow(commentId);

        comment.validateNoticeOwnership(noticeId);
        comment.validateOwner(user);

        handleImageUpdate(comment, request);
        comment.update(request.getContent());

        return CommentResponse.from(comment);
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

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글이 존재하지 않습니다."));
    }
}