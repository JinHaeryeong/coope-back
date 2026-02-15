package com.coope.server.domain.chat.controller;

import com.coope.server.domain.chat.dto.MessageRequest;
import com.coope.server.domain.chat.dto.MessageResponse;
import com.coope.server.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/send")
    public void sendMessage(MessageRequest request) {
        log.info("STOMP 메시지 수신: 방={}, 보낸이={}", request.getRoomId(), request.getSenderId());

        MessageResponse response = chatService.saveMessage(request);

        messagingTemplate.convertAndSend("/topic/chat/" + request.getRoomId(), response);
    }
}