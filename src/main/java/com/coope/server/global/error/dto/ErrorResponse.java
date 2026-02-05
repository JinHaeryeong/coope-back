package com.coope.server.global.error.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    private final boolean success;
    private final String message;

    public static ErrorResponse fail(String message) {
        return new ErrorResponse(false, message);
    }
}
