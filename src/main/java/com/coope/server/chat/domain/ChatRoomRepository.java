package com.coope.server.chat.domain;

import java.util.Optional;

public interface ChatRoomRepository {
    Optional<ChatRoom> findById(Long id);
    ChatRoom save(ChatRoom chatRoom);
    void delete(ChatRoom chatRoom);
}
