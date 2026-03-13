package com.coope.server.ai.presentation.dto;

public record VoiceProcessResponse(
        String transcript,
        String summary
) {}