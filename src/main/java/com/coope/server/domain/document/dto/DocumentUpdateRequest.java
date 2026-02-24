package com.coope.server.domain.document.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentUpdateRequest {
    private String title;
    private String content;
    private String icon;
    private String coverImage;
}