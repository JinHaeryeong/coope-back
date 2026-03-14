package com.coope.server.shared.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileEventListener {

    private final FileService fileService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFileDelete(FileDeleteEvent event) {
        log.info("파일 삭제 이벤트 처리 시작 - URL: {}, 카테고리: {}", event.imageUrl(), event.category());
        try {
            boolean isDeleted = fileService.deleteFile(event.imageUrl(), event.category());
            if (!isDeleted) log.warn("파일 삭제 실패 - URL: {}", event.imageUrl());
        } catch (Exception e) {
            log.error("파일 삭제 중 예외 발생 - URL: {}, Error: {}", event.imageUrl(), e.getMessage());
        }
    }
}
