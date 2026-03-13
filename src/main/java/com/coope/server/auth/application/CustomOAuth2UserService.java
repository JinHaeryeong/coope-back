package com.coope.server.auth.application;

import com.coope.server.auth.domain.GoogleUserInfo;
import com.coope.server.auth.domain.OAuth2UserInfo;
import com.coope.server.user.domain.User;
import com.coope.server.user.domain.UserRepository;
import com.coope.server.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.createSocialUser(userInfo)));

        return new UserDetailsImpl(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo getOAuth2UserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (registrationId.equals("google")) {
            return new GoogleUserInfo(oAuth2User.getAttributes());
        }

        throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_provider"), "지원하지 않는 로그인 제공자입니다."
        );
    }
}
