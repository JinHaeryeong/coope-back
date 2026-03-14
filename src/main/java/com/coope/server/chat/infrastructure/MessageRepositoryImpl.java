package com.coope.server.chat.infrastructure;

import com.coope.server.chat.domain.Message;
import com.coope.server.chat.domain.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository messageJpaRepository;

    @Override
    public Slice<Message> findByChatRoomIdCursor(Long roomId, Long lastMessageId, Pageable pageable) {
        return messageJpaRepository.findByChatRoomIdCursor(roomId, lastMessageId, pageable);
    }

    @Override
    public Message save(Message message) {
        return messageJpaRepository.save(message);
    }
}
