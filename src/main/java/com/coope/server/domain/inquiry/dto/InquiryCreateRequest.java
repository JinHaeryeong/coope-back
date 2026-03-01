package com.coope.server.domain.inquiry.dto;

import com.coope.server.domain.inquiry.enums.InquiryCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "문의사항 생성 요청")
public class InquiryCreateRequest {

    @NotBlank(message = "문의 제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해 주세요.")
    @Schema(description = "문의 제목", example = "프로필 이미지가 안 바뀌어요.")
    private String title;

    @NotBlank(message = "문의 내용은 필수입니다.")
    @Schema(description = "문의 내용", example = "갤러리에서 사진을 선택해도 반응이 없습니다.")
    private String content;

    @NotNull(message = "카테고리를 선택해 주세요.")
    @Schema(description = "카테고리", example = "ERROR")
    private InquiryCategory category;

    @NotBlank(message = "사용 환경 정보가 필요합니다.")
    @Schema(description = "사용 환경", example = "iOS")
    private String environment;

    @Schema(description = "첨부 파일 (최대 5개 등 제한 가능)")
    private List<MultipartFile> files;
}