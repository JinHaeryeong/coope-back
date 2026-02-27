package com.coope.server.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ProfileUpdateFullRequest {
    private String nickname;            // 닉네임 (Null 가능)
    private MultipartFile profileImage;  // 프사 (Null 가능)

    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "현재 비밀번호 형식이 올바르지 않습니다."
    )
    private String currentPassword;

    @Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "새 비밀번호는 영어, 숫자, 특수문자를 포함해야 합니다."
    )
    private String newPassword;
}