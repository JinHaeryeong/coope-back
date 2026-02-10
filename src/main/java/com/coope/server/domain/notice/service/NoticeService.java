package com.coope.server.domain.notice.service;

import com.coope.server.domain.notice.dto.NoticeDetailResponse;
import com.coope.server.domain.notice.dto.NoticeResponse;
import com.coope.server.domain.notice.dto.NoticeWriteRequest;
import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.notice.repository.NoticeRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.enums.Role;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.error.exception.FileStorageException;
import com.coope.server.global.error.exception.NoticeNotFoundException;
import com.coope.server.global.infra.ImageCategory;
import com.coope.server.global.infra.LocalFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final LocalFileService localFileService;

    public Page<NoticeResponse> getAllNotices(Pageable pageable) {

        return noticeRepository.findAll(pageable)
                .map(NoticeResponse::from);
    }

    @Transactional
    public NoticeResponse createNotice(NoticeWriteRequest request, User user, MultipartFile file) {
        String savedImageUrl = null;
        if (file != null && !file.isEmpty()) {
            savedImageUrl = localFileService.upload(file, ImageCategory.NOTICE);
        }

        Notice notice = request.toEntity(user, savedImageUrl);
        Notice savedNotice = noticeRepository.save(notice);

        return NoticeResponse.from(savedNotice);
    }

    public NoticeDetailResponse getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항을 찾을 수 없습니다."));
        return NoticeDetailResponse.from(notice);
    }

    @Transactional
    public void increaseViewCount(Long id) {
        int updatedCount = noticeRepository.updateViews(id);

        if (updatedCount == 0) {
            throw new NoticeNotFoundException("해당 공지사항을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeWriteRequest requestDto, User user) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항이 존재하지 않습니다."));

        if (!user.getRole().equals(Role.ROLE_ADMIN)) {
            throw new AccessDeniedException("공지사항 수정 권한이 없습니다.");
        }

        String currentImageUrl = notice.getImageUrl();

        if (requestDto.isDeleteImage() || (requestDto.getFile() != null && !requestDto.getFile().isEmpty())) {
            if (currentImageUrl != null) {
                boolean isDeleted = localFileService.deleteFile(currentImageUrl, ImageCategory.NOTICE);
                if (!isDeleted) {
                    throw new FileStorageException("기존 이미지 삭제에 실패하여 수정을 완료할 수 없습니다: " + currentImageUrl);
                }
                notice.updateImageUrl(null);
            }
        }

        if (requestDto.getFile() != null && !requestDto.getFile().isEmpty()) {
            String newImageUrl = localFileService.upload(requestDto.getFile(), ImageCategory.NOTICE);
            notice.updateImageUrl(newImageUrl);
        }

        notice.update(requestDto.getTitle(), requestDto.getContent());

        return NoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항을 찾을 수 없습니다."));

        String currentImageUrl = notice.getImageUrl();
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            boolean isDeleted = localFileService.deleteFile(currentImageUrl, ImageCategory.NOTICE);

            if (!isDeleted) {
                throw new FileStorageException("파일 삭제에 실패하여 삭제를 완료할 수 없습니다." + currentImageUrl);
            }
        }

        noticeRepository.delete(notice);
    }

}