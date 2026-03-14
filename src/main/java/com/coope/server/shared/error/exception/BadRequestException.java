package com.coope.server.shared.error.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
