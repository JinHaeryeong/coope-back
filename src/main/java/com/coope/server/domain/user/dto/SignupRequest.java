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

    @NotBlank @Email
    private String email; // final 제거

    @NotBlank @Size(min = 8)
    private String password; // final 제거

    @NotBlank
    private String name; // final 제거

    @NotBlank
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