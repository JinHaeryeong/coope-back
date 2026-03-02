package com.coope.server.domain.inquiry.controller;

import com.coope.server.domain.inquiry.dto.InquiryAnswerRequest;
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
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "BearerAuth")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의사항 등록", description = "이미지를 포함한 문의사항을 등록합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @ModelAttribute InquiryCreateRequest request
    ) {
        Long inquiryId = inquiryService.createInquiry(userDetails.getUser().getId(), request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(inquiryId);
    }

    @Operation(summary = "문의사항 답변 등록", description = "관리자가 특정 문의사항에 대한 답변을 등록합니다.")
    @PostMapping("/{id}/answer")
    public ResponseEntity<Void> createAnswer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody InquiryAnswerRequest request
    ) {
        inquiryService.createAnswer(id, userDetails.getUser().getId(), request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).build();
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

    @Operation(summary = "전체 문의 내역 조회 (관리자 전용)", description = "모든 사용자의 문의 내역을 최신순으로 페이징 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<Page<InquiryResponse>> getAllInquiries(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<InquiryResponse> responses = inquiryService.getAllInquiries(pageable);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "문의사항 상세 조회", description = "문의사항의 상세 내용과 첨부 파일 목록을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(inquiryService.getInquiry(id, userDetails.getUser().getId(), userDetails.getUser().getRole()));
    }

    @Operation(summary = "문의사항 삭제", description = "문의사항과 관련된 첨부 파일을 서버에서 삭제하고 DB 데이터를 제거합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        inquiryService.deleteInquiry(id, userDetails.getUser().getId(), userDetails.getUser().getRole());
        return ResponseEntity.noContent().build();
    }
}