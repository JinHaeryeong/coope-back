package com.coope.server.inquiry.presentation;

import com.coope.server.inquiry.application.InquiryService;
import com.coope.server.inquiry.application.dto.InquiryAnswerRequest;
import com.coope.server.inquiry.application.dto.InquiryCreateRequest;
import com.coope.server.inquiry.application.dto.InquiryResponse;
import com.coope.server.shared.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Inquiry", description = "문의사항 API")
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "BearerAuth")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의사항 등록")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @ModelAttribute InquiryCreateRequest request
    ) {
        Long inquiryId = inquiryService.createInquiry(
                userDetails.getUser().getId(),
                request.getTitle(),
                request.getContent(),
                request.getCategory(),
                request.getEnvironment(),
                request.getFiles()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(inquiryId);
    }

    @Operation(summary = "문의사항 답변 등록")
    @PostMapping("/{id}/answers")
    public ResponseEntity<Void> createAnswer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody InquiryAnswerRequest request
    ) {
        inquiryService.createAnswer(id, userDetails.getUser().getId(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "내 문의 내역 조회")
    @GetMapping("/me")
    public ResponseEntity<Page<InquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(inquiryService.getMyInquiries(userDetails.getUser().getId(), pageable));
    }

    @Operation(summary = "전체 문의 내역 조회 (관리자 전용)")
    @GetMapping
    public ResponseEntity<Page<InquiryResponse>> getAllInquiries(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(inquiryService.getAllInquiries(pageable));
    }

    @Operation(summary = "문의사항 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(inquiryService.getInquiry(
                id, userDetails.getUser().getId(), userDetails.getUser().getRole()));
    }

    @Operation(summary = "문의사항 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        inquiryService.deleteInquiry(id, userDetails.getUser().getId(), userDetails.getUser().getRole());
        return ResponseEntity.noContent().build();
    }
}
