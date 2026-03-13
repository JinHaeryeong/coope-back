package com.coope.server.chat.presentation;

import com.coope.server.chat.application.ChatService;
import com.coope.server.chat.presentation.dto.MessageRequest;
import com.coope.server.chat.presentation.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/send")
    public void sendMessage(MessageRequest request, Principal principal) {
        Long authenticatedUserId = Long.parseLong(principal.getName());

        log.info("STOMP 메시지 수신: 방={}, 보낸이(검증됨)={}", request.getRoomId(), authenticatedUserId);

        MessageResponse response = chatService.saveMessage(request, authenticatedUserId);

        messagingTemplate.convertAndSend("/topic/chat/" + request.getRoomId(), response);
    }
}
