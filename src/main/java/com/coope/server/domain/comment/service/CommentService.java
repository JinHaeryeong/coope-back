package com.coope.server.domain.comment.service;

import com.coope.server.domain.comment.dto.CommentRequest;
import com.coope.server.domain.comment.dto.CommentResponse;
import com.coope.server.domain.comment.entity.Comment;
import com.coope.server.domain.comment.repository.CommentRepository;
import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.notice.repository.NoticeRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.error.exception.NoticeNotFoundException;
import com.coope.server.global.infra.LocalFileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final LocalFileService localFileService;

    @Transactional
    public CommentResponse createComment(Long noticeId, CommentRequest requestDto, User user) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항이 존재하지 않습니다."));

        String savedImageUrl = localFileService.upload(requestDto.getFile(), "comments");

        Comment comment = requestDto.toEntity(notice, user, savedImageUrl);
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    public List<CommentResponse> getComments(Long noticeId) {
        return commentRepository.findAllByNoticeIdOrderByCreatedAtDesc(noticeId)
                .stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }

        if (comment.getImageUrl() != null) {
            localFileService.deleteFile(comment.getImageUrl(), "comments");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest requestDto, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("해당 댓글이 존재하지 않습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("댓글 수정 권한이 없습니다.");
        }

        comment.update(requestDto.getContent());

        return CommentResponse.from(comment);
    }
}