package com.coope.server.chat.infrastructure;

import com.coope.server.chat.domain.ChatRoom;
import com.coope.server.chat.domain.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    private final ChatRoomJpaRepository chatRoomJpaRepository;

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return chatRoomJpaRepository.findById(id);
    }

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return chatRoomJpaRepository.save(chatRoom);
    }

    @Override
    public void delete(ChatRoom chatRoom) {
        chatRoomJpaRepository.delete(chatRoom);
    }
}
