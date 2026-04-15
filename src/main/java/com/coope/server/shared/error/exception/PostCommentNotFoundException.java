package com.coope.server.shared.error.exception;

public class PostCommentNotFoundException extends RuntimeException {
    public PostCommentNotFoundException(String message) {
        super(message);
    }
}
