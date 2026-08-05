package io.github.kaltz.feldbuch.knowledge.entity;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum KnowledgeRootCategory {

    COMPUTER_SCIENCE("컴퓨터 과학"),
    PROGRAMMING_LANGUAGE("프로그래밍 언어"),
    WEB_DEVELOPMENT("웹 개발"),
    DATABASE("데이터베이스"),
    NETWORK("네트워크"),
    OPERATING_SYSTEM("운영체제"),
    CLOUD("클라우드"),
    DEVOPS("DevOps"),
    ARTIFICIAL_INTELLIGENCE("인공지능"),
    SECURITY("보안"),
    COMPUTER_USAGE("컴퓨터 사용"),
    COMMUNICATION("커뮤니케이션");

    private final String displayName;

    KnowledgeRootCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static KnowledgeRootCategory fromDisplayName(String displayName) {
        if (
                displayName == null
                        || displayName.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "지식 대분류 이름은 필수입니다."
            );
        }

        String normalized =
                displayName.trim();

        return Arrays.stream(values())
                .filter(category ->
                        category.displayName.equals(
                                normalized
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "지원하지 않는 지식 대분류입니다: "
                                        + displayName
                        )
                );
    }

    public static String toPromptList() {
        return Arrays.stream(values())
                .map(category ->
                        "- "
                                + category.name()
                                + ": "
                                + category.displayName
                )
                .collect(
                        Collectors.joining(
                                System.lineSeparator()
                        )
                );
    }
}
