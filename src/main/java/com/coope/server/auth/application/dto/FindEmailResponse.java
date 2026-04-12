package com.coope.server.auth.application.dto;

import com.coope.server.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindEmailResponse {

    private final String maskedEmail;
    private final String provider; // LOCAL, GOOGLE 등

    public static FindEmailResponse from(User user) {
        return FindEmailResponse.builder()
                .maskedEmail(maskEmail(user.getEmail()))
                .provider(user.getProvider().name())
                .build();
    }

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            // 앞자리가 너무 짧으면 첫 글자만 노출
            return email.charAt(0) + "***" + email.substring(atIndex);
        }
        // 앞 2자리 노출, 나머지 마스킹
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
