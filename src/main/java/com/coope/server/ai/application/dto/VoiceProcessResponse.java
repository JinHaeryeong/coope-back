package com.coope.server.ai.application.dto;

public record VoiceProcessResponse(
        String transcript,
        String summary
) {}