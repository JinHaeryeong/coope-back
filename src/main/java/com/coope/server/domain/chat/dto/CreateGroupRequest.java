package com.coope.server.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateGroupRequest {
    private List<Long> userIds;
    private String roomName;
}