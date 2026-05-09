package com.coope.server.community.domain.post.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 프론트엔드 TECH_STACKS 상수와 1:1 매핑되는 기술 스택 Enum
 * 허용된 값 외 입력 시 역직렬화 단계에서 예외 발생
 */
public enum TechStack {

    REACT("React"),
    NEXT_JS("Next.js"),
    VUE("Vue"),
    TYPESCRIPT("TypeScript"),
    JAVASCRIPT("JavaScript"),
    SPRING("Spring"),
    SPRING_BOOT("Spring Boot"),
    JAVA("Java"),
    KOTLIN("Kotlin"),
    NODE_JS("Node.js"),
    NEST_JS("NestJS"),
    MYSQL("MySQL"),
    MONGODB("MongoDB"),
    REDIS("Redis"),
    DOCKER("Docker"),
    AWS("AWS"),
    GITHUB_ACTIONS("GitHub Actions"),
    FIREBASE("Firebase"),
    TAILWIND_CSS("TailwindCSS"),
    REACT_QUERY("React Query"),
    C("C"),
    C_SHARP("C#"),
    C_PLUS_PLUS("C++"),
    PYTHON("Python");

    private final String displayName;

    TechStack(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * JSON 역직렬화 시 displayName 기준으로 매핑
     * 예: "Spring Boot" -> SPRING_BOOT
     */
    @JsonCreator
    public static TechStack from(String value) {
        for (TechStack stack : values()) {
            if (stack.displayName.equalsIgnoreCase(value)) {
                return stack;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 기술 스택입니다: " + value);
    }
}
