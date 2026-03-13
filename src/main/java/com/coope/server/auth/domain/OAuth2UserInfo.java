package com.coope.server.auth.domain;

import com.coope.server.user.domain.enums.Provider;

public interface OAuth2UserInfo {
    Provider getProvider();      // google, naver, kakao 등
    String getProviderId();    // 소셜 서비스의 고유 ID
    String getEmail();
    String getName();
    String getPicture();
}