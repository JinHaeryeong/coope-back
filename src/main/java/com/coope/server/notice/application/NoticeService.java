package com.coope.server.notice.application;

import com.coope.server.notice.domain.Notice;
import com.coope.server.notice.domain.NoticeRepository;
import com.coope.server.notice.application.dto.NoticeDetailResponse;
import com.coope.server.notice.application.dto.NoticeResponse;
import com.coope.server.notice.application.dto.NoticeWriteRequest;
import com.coope.server.shared.file.FileDeleteEvent;
import com.coope.server.shared.file.FileRollbackDeleteEvent;
import com.coope.server.user.domain.User;
import com.coope.server.shared.error.exception.NoticeNotFoundException;
import com.coope.server.shared.file.FileService;
import com.coope.server.shared.file.ImageCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final FileService fileService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private static final String VIEW_COUNT_KEY = "notice:views:";

    public Page<NoticeResponse> getAllNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable)
                .map(NoticeResponse::from);
    }

    @Transactional
    public NoticeResponse createNotice(NoticeWriteRequest request, User user, MultipartFile file) {
        String savedImageUrl = null;
        if (file != null && !file.isEmpty()) {
            savedImageUrl = fileService.upload(file, ImageCategory.NOTICE);

            eventPublisher.publishEvent(
                    new FileRollbackDeleteEvent(savedImageUrl, ImageCategory.NOTICE)
            );
        }

        Notice notice = Notice.createNotice(request.getTitle(), request.getContent(), savedImageUrl, user);
        Notice savedNotice = noticeRepository.save(notice);

        return NoticeResponse.from(savedNotice);
    }

    public NoticeDetailResponse getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항을 찾을 수 없습니다."));

        String key = VIEW_COUNT_KEY + id;
        Object redisValue = redisTemplate.opsForValue().get(key);
        int redisViews = (redisValue != null) ? Integer.parseInt(redisValue.toString()) : 0;

        return NoticeDetailResponse.from(notice, redisViews);
    }

    public void increaseViewCount(Long id) {
        redisTemplate.opsForValue().increment(VIEW_COUNT_KEY + id);
    }

    @Transactional
    public NoticeResponse updateNotice(Long noticeId, NoticeWriteRequest request, User user) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항이 존재하지 않습니다."));

        processImageUpdate(notice, request);
        notice.update(request.getTitle(), request.getContent());

        return NoticeResponse.from(notice);
    }

    private void processImageUpdate(Notice notice, NoticeWriteRequest dto) {
        boolean hasNewFile = dto.getFile() != null && !dto.getFile().isEmpty();
        boolean shouldDeleteExisting = Boolean.TRUE.equals(dto.getDeleteImage()) || hasNewFile;

        if (!shouldDeleteExisting) return;

        if (notice.getImageUrl() != null) {
            eventPublisher.publishEvent(new FileDeleteEvent(notice.getImageUrl(), ImageCategory.NOTICE));
            notice.removeImage();
        }

        if (hasNewFile) {
            String newUrl = fileService.upload(dto.getFile(), ImageCategory.NOTICE);
            eventPublisher.publishEvent(
                    new FileRollbackDeleteEvent(newUrl, ImageCategory.NOTICE)
            );
            notice.changeImage(newUrl);
        }
    }


    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new NoticeNotFoundException("해당 공지사항을 찾을 수 없습니다."));

        String currentImageUrl = notice.getImageUrl();
        noticeRepository.delete(notice);

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            eventPublisher.publishEvent(new FileDeleteEvent(currentImageUrl, ImageCategory.NOTICE));
        }
    }
}
