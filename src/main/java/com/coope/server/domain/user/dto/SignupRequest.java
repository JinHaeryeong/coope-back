package com.coope.server.domain.user.dto;

import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.enums.Provider;
import com.coope.server.domain.user.enums.Role;
import lombok.*; // NoArgsConstructor, AllArgsConstructor 추가를 위해
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email; // final 제거

    @NotBlank @Size(min = 8)
    private String password; // final 제거

    @NotBlank
    @Size(min = 2, max = 20, message = "이름은 2~20자 이내여야 합니다.")
    private String name; // final 제거

    @NotBlank
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣 ]{2,20}$",
            message = "닉네임은 특수문자를 제외한 2~20자 이내여야 합니다."
    )
    private String nickname; // final 제거

    private MultipartFile userIcon; // final 제거

    // 서비스에서 호출할 엔티티 변환 메서드
    public User toEntity(String encodedPassword, String profileImageUrl) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .name(this.name)
                .nickname(this.nickname)
                .userIcon(profileImageUrl)
                .provider(Provider.LOCAL)
                .role(Role.ROLE_USER)
                .build();
    }
}