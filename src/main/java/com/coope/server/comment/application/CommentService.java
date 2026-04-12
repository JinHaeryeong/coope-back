package com.coope.server.comment.application;

import com.coope.server.comment.domain.Comment;
import com.coope.server.comment.domain.CommentRepository;
import com.coope.server.comment.application.dto.CommentResponse;
import com.coope.server.notice.domain.Notice;
import com.coope.server.notice.domain.NoticeRepository;
import com.coope.server.shared.file.FileRollbackDeleteEvent;
import com.coope.server.user.domain.User;
import com.coope.server.shared.error.exception.CommentNotFoundException;
import com.coope.server.shared.error.exception.NoticeNotFoundException;
import com.coope.server.shared.file.FileDeleteEvent;
import com.coope.server.shared.file.FileService;
import com.coope.server.shared.file.ImageCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public CommentResponse createComment(Long noticeId, String content, MultipartFile file, User user) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항이 존재하지 않습니다."));

        String savedImageUrl = null;

        if (file != null && !file.isEmpty()) {
            savedImageUrl = fileService.upload(file, ImageCategory.COMMENT);

            eventPublisher.publishEvent(
                    new FileRollbackDeleteEvent(savedImageUrl, ImageCategory.COMMENT)
            );
        }

        Comment comment = Comment.createComment(notice, user, content, savedImageUrl);
        return CommentResponse.from(commentRepository.save(comment));
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
    public CommentResponse updateComment(Long noticeId, Long commentId,
                                         String content, MultipartFile file, Boolean deleteImage, User user) {
        Comment comment = findCommentOrThrow(commentId);

        comment.validateNoticeOwnership(noticeId);
        comment.validateOwner(user);

        handleImageUpdate(comment, file, deleteImage);
        comment.update(content);

        return CommentResponse.from(comment);
    }

    private void handleImageUpdate(Comment comment, MultipartFile file, Boolean deleteImage) {
        String currentImageUrl = comment.getImageUrl();
        boolean hasNewFile = file != null && !file.isEmpty();

        if (Boolean.TRUE.equals(deleteImage) || hasNewFile) {
            if (currentImageUrl != null) {
                eventPublisher.publishEvent(new FileDeleteEvent(currentImageUrl, ImageCategory.COMMENT));
                comment.updateImage(null);
            }
        }
        if (hasNewFile) {
            String newUrl = fileService.upload(file, ImageCategory.COMMENT);
            eventPublisher.publishEvent(
                    new FileRollbackDeleteEvent(newUrl, ImageCategory.COMMENT)
            );
            comment.updateImage(newUrl);
        }
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글이 존재하지 않습니다."));
    }
}
