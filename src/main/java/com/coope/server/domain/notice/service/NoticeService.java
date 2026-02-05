package com.coope.server.domain.notice.service;

import com.coope.server.domain.notice.dto.NoticeDetailResponse;
import com.coope.server.domain.notice.dto.NoticeResponse;
import com.coope.server.domain.notice.dto.NoticeWriteRequest;
import com.coope.server.domain.notice.entity.Notice;
import com.coope.server.domain.notice.repository.NoticeRepository;
import com.coope.server.domain.user.entity.User;
import com.coope.server.global.infra.LocalFileService;
import jakarta.persistence.EntityNotFoundException;
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
        // 1. 파일 업로드 처리 (파일이 있을 경우에만 진행)
        String savedImageUrl = null;
        if (file != null && !file.isEmpty()) {
            // "notices" 폴더에 저장하도록 구분
            savedImageUrl = localFileService.upload(file, "notices");
        }

        Notice notice = request.toEntity(user, savedImageUrl);
        Notice savedNotice = noticeRepository.save(notice);

        return NoticeResponse.from(savedNotice);
    }

    public NoticeDetailResponse getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 공지사항을 찾을 수 없습니다."));
        return NoticeDetailResponse.from(notice);
    }

    @Transactional
    public void increaseViewCount(Long id) {
        int updatedCount = noticeRepository.updateViews(id);

        if (updatedCount == 0) {
            throw new EntityNotFoundException("해당 공지사항을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("글이 없습니다."));

        noticeRepository.delete(notice);
    }
}