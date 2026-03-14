package com.coope.server.shared.error.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) { super(message); }
}
