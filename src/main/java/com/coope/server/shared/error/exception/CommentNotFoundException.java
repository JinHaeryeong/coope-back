package com.coope.server.shared.error.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) { super(message); }
}
