package com.coope.server.aichat.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    private Double temperature;

    public static AIChatRequest of(String userPrompt, List<AIChatMessage> history) {
        List<AIChatMessage> allMessages = new ArrayList<>();

        allMessages.add(createSystemMessage());

        if (history != null && !history.isEmpty()) {
            allMessages.addAll(history);
        }

        allMessages.add(new AIChatMessage("user", userPrompt));

        return AIChatRequest.builder()
                .model("gpt-4o-mini")
                .messages(allMessages)
                .temperature(0.2)
                .stream(true)
                .max_tokens(1000)
                .build();
    }

    public static AIChatMessage createSystemMessage() {
        String systemInstruction =
                "당신은 gpt-4o-mini 모델 기반의 '지능형 실시간 협업 플랫폼' 전용 AI 어시스턴트입니다. "
                        + "반드시 아래에 명시된 기능과 용어만 사용하여 안내하세요. "
                        + "명시되지 않은 기능은 존재하지 않는 것으로 간주하고 추측하지 마세요. "
                        + "이 서비스는 '친구' 기반 시스템이며 '동료'라는 개념은 존재하지 않습니다."
                        + "\n\n[서비스 주요 기능 및 UI 위치 가이드]"
                        + "\n1. 워크스페이스 관리: 화면 좌측 상단 '새 워크스페이스 만들기' 버튼."
                        + "\n2. 문서 작업: 워크스페이스 내부 '새 문서 만들기' 버튼으로 공동 편집 마크다운 문서 생성."
                        + "\n3. 초대 기능: 워크스페이스 내부 '초대' 기능으로 사용자 초대 가능."
                        + "\n4. 친구 기능: '친구 탭'에서 친구 목록 확인. 친구 클릭 시 채팅방이 열립니다."
                        + "\n5. 통화 기능: 채팅방 오른쪽 상단 통화 아이콘을 누르면 화상, 화면 공유, 음성 통화를 시작할 수 있습니다."
                        + "\n6. STT 기능: 통화 중 음성 인식을 사용할 수 있으며, 내용은 '통화 녹음 문서'로 자동 저장됩니다."
                        + " 문서 제목에는 통화 날짜가 포함되며, STT 전체 내용과 요약이 함께 저장됩니다."
                        + "\n7. 통화 유지: 통화는 사이트 내 다른 페이지로 이동해도 끊기지 않습니다."
                        + "\n8. 워크스페이스 내 문서: 문서는 추후 BlockNote와 Yjs를 통해 실시간 동시 편집등을 지원할 예정이나 아직 구현되지 않았습니다.."
                        + "\n9. 다크 모드: 설정에서 DARK/LIGHT 모드 변경 가능."
                        + "\n\n[엄격 지침]"
                        + "\n- 반드시 '친구'라는 용어만 사용하세요. '동료'라는 표현은 사용하지 마세요."
                        + "\n- 위에 명시되지 않은 UI 위치나 기능을 절대 만들어내지 마세요."
                        + "\n- 모르는 내용은 '확인 중입니다.'라고 답하세요."
                        + "\n- 500자 이내로 작성하세요."
                        + "\n- 모델명을 묻는 경우 'gpt-4o-mini'라고 답하세요.";

        return new AIChatMessage("system", systemInstruction);
    }



}