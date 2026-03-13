package com.coope.server.global.liveblocks.service;

import com.coope.server.user.domain.User;
import com.coope.server.workspace.domain.WorkspaceMember;
import com.coope.server.workspace.domain.WorkspaceMemberRepository;
import com.coope.server.global.error.exception.AccessDeniedException;
import com.coope.server.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveblocksAuthService {

    @Value("${liveblocks.secret-key}")
    private String liveblocksSecretKey;

    private final RestTemplate restTemplate;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    private static final String[] CURSOR_COLORS = {
            "#FF5733", "#33FF57", "#3357FF", "#F333FF", "#FF33A1",
            "#33FFF5", "#FF8333", "#8B5CF6", "#10B981", "#F59E0B"
    };

    public String getAuthorizedToken(User user, String roomId) {
        String workspaceCode = extractWorkspaceCode(roomId);

        boolean isAdmin = "ROLE_ADMIN".equals(user.getRole().name());
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceInviteCodeAndUserId(workspaceCode, user.getId())
                .orElseThrow(() -> new AccessDeniedException("워크스페이스 멤버가 아닙니다."));

        boolean canWrite = isAdmin || member.isEditor();

        Map<String, Object> userInfo = createUserInfo(user);

        return generateLiveblocksToken(user.getId().toString(), userInfo, roomId, canWrite);
    }

    public String generateLiveblocksToken(String userId, Map<String, Object> userInfo, String roomId, boolean canWrite) {
        String url = "https://api.liveblocks.io/v2/authorize-user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + liveblocksSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        if (userInfo != null) {
            body.put("userInfo", userInfo);
        }

        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put(roomId, canWrite
                ? List.of("room:write", "room:read", "room:presence:write", "comments:write")
                : List.of("room:read", "room:presence:write")
        );
        body.put("permissions", permissions);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {}
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("token");
        } else {
            log.error("Liveblocks 토큰 발급 실패. Status: {}", response.getStatusCode());
            throw new RuntimeException("Liveblocks 토큰 발급 실패");
        }
    }

    private String extractWorkspaceCode(String roomId) {
        if (roomId == null || !roomId.startsWith("doc-")) {
            throw new BadRequestException("유효하지 않은 roomId 형식입니다.");
        }
        String[] parts = roomId.split("-");
        if (parts.length < 3) {
            throw new BadRequestException("roomId에서 워크스페이스 코드를 추출할 수 없습니다.");
        }
        return parts[1]; // doc-{workspaceCode}-{documentId} 구조 기준
    }

    private Map<String, Object> createUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        String email = user.getEmail();
        userInfo.put("name", email.split("@")[0]);

        int colorIndex = Math.abs(email.hashCode()) % CURSOR_COLORS.length;
        userInfo.put("color", CURSOR_COLORS[colorIndex]);
        return userInfo;
    }
}