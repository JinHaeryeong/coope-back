package com.coope.server.domain.inquiry.controller;

import com.coope.server.domain.inquiry.dto.InquiryCreateRequest;
import com.coope.server.domain.inquiry.dto.InquiryResponse;
import com.coope.server.domain.inquiry.service.InquiryService;
import com.coope.server.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inquiry", description = "문의사항 API")
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의사항 등록", description = "이미지를 포함한 문의사항을 등록합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @ModelAttribute InquiryCreateRequest request
    ) {
        Long inquiryId = inquiryService.createInquiry(userDetails.getUser().getId(), request);
        return ResponseEntity.ok(inquiryId);
    }

    @Operation(summary = "내 문의 내역 조회", description = "본인이 작성한 문의 내역을 최신순으로 페이징 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<Page<InquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<InquiryResponse> responses = inquiryService.getMyInquiries(userDetails.getUser().getId(), pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getInquiry(@PathVariable Long id) {
        return ResponseEntity.ok(inquiryService.getInquiry(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.noContent().build();
    }
}