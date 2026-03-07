package com.coope.server.global.liveblocks.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
@Service
public class LiveblocksAuthService {

    @Value("${liveblocks.secret-key}")
    private String liveblocksSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();

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

        // 권한: roomId 키로 직접 지정 (Liveblocks가 지원하는 방식)
        Map<String, List<String>> permissions = new HashMap<>();
        permissions.put(roomId, canWrite
                ? List.of("room:write", "room:read", "room:presence:write", "comments:write")
                : List.of("room:read", "room:presence:write")
        );
        body.put("permissions", permissions);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("token");
        } else {
            throw new RuntimeException("Liveblocks 토큰 발급 실패: " + response.getStatusCode().value());
        }
    }
}