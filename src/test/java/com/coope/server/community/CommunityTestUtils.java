package com.coope.server.community;

import com.coope.server.user.domain.User;
import com.coope.server.user.domain.enums.Role;
import org.springframework.test.util.ReflectionTestUtils;

public class CommunityTestUtils {
    public static User createTestUser(Long id, String nickname, Role role) {
        User user = User.builder()
                .email(nickname + "@test.com")
                .nickname(nickname)
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
