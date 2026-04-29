package com.coope.server.community.presentation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LikeToggleResponse {

    private final boolean liked;

    public static LikeToggleResponse of(boolean liked) {
        return new LikeToggleResponse(liked);
    }
}
