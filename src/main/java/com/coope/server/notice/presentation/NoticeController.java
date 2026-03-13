package com.coope.server.notice.presentation;

import com.coope.server.notice.application.NoticeService;
import com.coope.server.notice.presentation.dto.NoticeDetailResponse;
import com.coope.server.notice.presentation.dto.NoticeResponse;
import com.coope.server.notice.presentation.dto.NoticeWriteRequest;
import com.coope.server.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<Page<NoticeResponse>> getAllNotices(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getAllNotices(pageable));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @ModelAttribute NoticeWriteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noticeService.createNotice(request, userDetails.getUser(), request.getFile()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute NoticeWriteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(noticeService.updateNotice(id, request, userDetails.getUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(id));
    }

    @PatchMapping("/{id}/views")
    public ResponseEntity<Void> increaseViewCount(@PathVariable("id") Long id) {
        noticeService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
