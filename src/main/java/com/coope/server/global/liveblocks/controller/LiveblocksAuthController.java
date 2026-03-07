package com.coope.server.global.liveblocks.controller;

import com.coope.server.domain.user.entity.User;
import com.coope.server.domain.workspace.entity.WorkspaceMember;
import com.coope.server.domain.workspace.repository.WorkspaceMemberRepository;
import com.coope.server.global.liveblocks.service.LiveblocksAuthService;
import com.coope.server.global.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LiveblocksAuthController {

    private final LiveblocksAuthService liveblocksAuthService;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    private static final String[] CURSOR_COLORS = {
            "#FF5733", "#33FF57", "#3357FF", "#F333FF", "#FF33A1",
            "#33FFF5", "#FF8333", "#8B5CF6", "#10B981", "#F59E0B"
    };

    public LiveblocksAuthController(LiveblocksAuthService liveblocksAuthService,
                                    WorkspaceMemberRepository workspaceMemberRepository) {
        this.liveblocksAuthService = liveblocksAuthService;
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @PostMapping("/liveblocks-auth")
    public ResponseEntity<Map<String, String>> authorize(@RequestBody Map<String, String> requestBody) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = userDetails.getUser();
        String userId = currentUser.getId().toString();
        String email = currentUser.getEmail();

        String roomId = requestBody.get("roomId");
        if (roomId == null || !roomId.startsWith("doc-")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or missing roomId"));
        }

        String[] parts = roomId.split("-");
        if (parts.length < 3) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid roomId format"));
        }
        String workspaceCode = parts[1];

        Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceInviteCodeAndUserId(
                workspaceCode, currentUser.getId()
        );

        boolean canEdit = false;
        if (memberOpt.isPresent()) {
            WorkspaceMember member = memberOpt.get();
            canEdit = member.isEditor();  // OWNER || EDITOR 권한 확인
        }

        // 관리자라면 무조건 편집 가능
        if ("ROLE_ADMIN".equals(currentUser.getRole().name())) {
            canEdit = true;
        }

        Map<String, Object> userInfo = new HashMap<>();

        String displayName = email.split("@")[0];
        userInfo.put("name", displayName);

        int colorIndex = Math.abs(email.hashCode()) % CURSOR_COLORS.length;
        String userColor = CURSOR_COLORS[colorIndex];
        userInfo.put("color", userColor);

        String token = liveblocksAuthService.generateLiveblocksToken(userId, userInfo, roomId, canEdit);

        return ResponseEntity.ok(Map.of("token", token));
    }
}