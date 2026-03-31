package com.coope.server.shared.file;

public record FileRollbackDeleteEvent(
        String url,
        ImageCategory category
) {}
