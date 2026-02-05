package com.coope.server.domain.notice.controller;

import com.coope.server.domain.notice.dto.NoticeDetailResponse;
import com.coope.server.domain.notice.dto.NoticeResponse;
import com.coope.server.domain.notice.dto.NoticeWriteRequest;
import com.coope.server.domain.notice.service.NoticeService;
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


    @GetMapping("/all")
    public ResponseEntity<Page<NoticeResponse>> getAllNotices(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<NoticeResponse> noticePage = noticeService.getAllNotices(pageable);
        return ResponseEntity.ok(noticePage);
    }

    @PostMapping(value = "/write", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @ModelAttribute NoticeWriteRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        NoticeResponse response = noticeService.createNotice(request, userDetails.getUser(), request.getFile());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(@PathVariable("id") Long id) {
        NoticeDetailResponse response = noticeService.getNoticeDetail(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/detail/views/{id}")
    public ResponseEntity<Void> increaseViewCount(@PathVariable("id") Long id) {
        noticeService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/detail/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable("id")Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}