package com.coope.server.chat.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ChatUploadResponse {
    private final String fileUrl;
    private final String fileName;
    private final String fileFormat;

    @Builder
    private ChatUploadResponse(String fileUrl, String fileName, String fileFormat) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileFormat = fileFormat;
    }
}