package com.coope.server.user.presentation.dto;

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
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 이내여야 합니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣]{2,20}$",
            message = "닉네임은 특수문자를 제외한 한글, 영문, 숫자만 가능합니다."
    )
    private String nickname;
    private MultipartFile profileImage;  // 프사 (Null 가능)

    private String currentPassword;

    private String newPassword;

    boolean deleteProfileImage;
}