package com.coope.server.domain.aichat.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIChatRequest {
    private String model;
    private List<AIChatMessage> messages;
    private boolean stream;
    private Integer max_tokens;

    public static AIChatRequest createDefault(List<AIChatMessage> allMessages) {
        return AIChatRequest.builder()
                .model("gpt-4o-mini")
                .messages(allMessages)
                .stream(true)
                .max_tokens(1000)
                .build();
    }

    public static  AIChatMessage createSystemMessage() {
        String systemInstruction =
                "당신은 gpt-4o-mini 모델을 기반으로 한 웹 애플리케이션의 AI 어시스턴트입니다. "
                        + "이 웹사이트는 문서 관리와 협업을 위한 플랫폼입니다. "
                        + "주요 기능으로는: 문서 생성 및 관리, 실시간 협업, 검색 기능, AI 어시스턴트(당신) 등이 있습니다. "
                        + "사용자의 질문에 대해 웹사이트의 기능과 관련된 정확하고 상세한 답변을 제공해주세요. "
                        + "특히 버튼이나 UI 요소들의 기능에 대해 명확하게 설명해주세요. "
                        + "답변은 친절하고 전문적인 톤으로 작성해주세요. "
                        + "만약 사용자가 당신의 모델 버전에 대해 질문하면, gpt-4o-mini 모델을 사용한다고 정확하게 알려주세요."
                        + "답변은 핵심 위주로 간결하게 작성하고, 최대 3문단(약 500자)을 넘지 않도록 하세요. "
                        + "설명이 길어지면 불렛 포인트를 사용하세요.";
        return new AIChatMessage("system", systemInstruction);
    }




}