package com.coope.server.domain.auth.service;

import com.coope.server.domain.auth.oauth.GoogleUserInfo;
import com.coope.server.domain.auth.oauth.OAuth2UserInfo;
import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.user.enums.Provider;
import com.coope.server.domain.user.enums.Role;
import com.coope.server.domain.user.repository.UserRepository;
import com.coope.server.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        final OAuth2UserInfo userInfo = getOAuth2UserInfo(userRequest, oAuth2User);

        String email = userInfo.getEmail();
        Provider provider = userInfo.getProvider();
        String providerId = userInfo.getProviderId();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name(userInfo.getName())
                        .nickname(userInfo.getName())
                        .userIcon(userInfo.getPicture())
                        .role(Role.ROLE_USER)
                        .provider(provider)
                        .providerId(providerId)
                        .build()));

        return new UserDetailsImpl(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo getOAuth2UserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (registrationId.equals("google")) {
            return new GoogleUserInfo(oAuth2User.getAttributes());
        }

        throw new OAuth2AuthenticationException("지원하지 않는 로그인 제공자입니다.");
    }
}
