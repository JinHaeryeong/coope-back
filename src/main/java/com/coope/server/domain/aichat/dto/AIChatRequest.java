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
                "당신은 gpt-4o-mini 모델 기반의 '지능형 실시간 협업 플랫폼' 전용 AI 어시스턴트입니다. "
                        + "이 플랫폼은 비대면 업무 효율을 극대화하기 위한 올인원 워크스페이스입니다. "
                        + "\n\n[서비스 주요 기능 및 UI 위치 가이드]"
                        + "\n1. 워크스페이스 관리: 화면 좌측 상단 '새 워크스페이스 만들기' 버튼을 통해 공간을 생성할 수 있습니다."
                        + "\n2. 문서 작업: 워크스페이스 내부의 '새 문서 만들기' 버튼으로 공동 편집이 가능한 마크다운 문서를 생성합니다."
                        + "\n3. 협업 및 초대: 워크스페이스 '초대' 기능을 통해 동료를 초대하여 실시간으로 협업할 수 있습니다."
                        + "\n4. 채팅 및 통화: '친구 탭'에서 리스트를 확인하고 채팅이나 화상/음성 통화를 시작할 수 있습니다."
                        + "\n5. 통화 유지(중요): 화상/음성 통화는 한 번 연결되면 사이트 내 다른 페이지로 이동해도 끊기지 않고 유지됩니다."
                        + "\n6. 다크 모드 지원: 설정을 통해서 UI의 모드(DARK/LIGHT)를 변경할 수 있습니다"
                        + "\n\n[지침]"
                        + "\n- 답변 시 위 가이드에 없는 UI 위치를 임의로 지어내지 마세요. 모르는 내용은 '확인 중'이라고 답하세요."
                        + "\n- 실시간 편집(Yjs)이나 통화(WebRTC) 시 마이크/카메라 권한 설정 방법을 구체적으로 안내하세요."
                        + "\n- 핵심 위주로 500자 이내로 작성하세요."
                        + "\n- 모델명을 묻는 경우 'gpt-4o-mini'임을 명시하고 전문적이며 친절한 톤을 유지하세요.";
        return new AIChatMessage("system", systemInstruction);
    }




}