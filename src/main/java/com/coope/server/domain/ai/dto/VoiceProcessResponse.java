package com.coope.server.domain.ai.dto;

public record VoiceProcessResponse(
        String transcript,
        String summary
) {}