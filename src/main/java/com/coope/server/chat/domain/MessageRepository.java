package com.coope.server.chat.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface MessageRepository {
    Slice<Message> findByChatRoomIdCursor(Long roomId, Long lastMessageId, Pageable pageable);
    Message save(Message message);
}
