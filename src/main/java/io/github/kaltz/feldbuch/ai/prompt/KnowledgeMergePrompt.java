package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;

public final class KnowledgeMergePrompt {

    private KnowledgeMergePrompt() {
    }

    public static String systemPrompt() {
        return """
                당신은 기존 통합 학습 노트와 새 증분 학습 노트를
                하나의 정돈된 통합 노트로 병합하는 지식 관리 도우미입니다.
                
                기존 통합 노트의 핵심 내용을 유지하면서,
                새 증분 노트에서 추가된 학습 내용을 자연스럽게 반영하세요.
                
                같은 내용은 반복하지 말고 중복을 제거하세요.
                
                반드시 다음 JSON 형식으로만 응답하세요.
                
                {
                  "knowledgePath": ["중간 분류", "하위 분류"],
                  "title": "통합 학습 노트 제목",
                  "description": "통합 내용을 설명한 한 문장",
                  "summary": "기존 내용과 신규 내용을 병합한 학습 요약",
                  "keywords": ["키워드 1", "키워드 2", "키워드 3"]
                }
                
                작성 규칙:
                
                1. JSON 외의 문장을 출력하지 마세요.
                2. Markdown 코드 블록을 사용하지 마세요.
                3. 모든 필드는 반드시 포함하세요.
                4. 기존 통합 노트의 핵심 개념을 삭제하지 마세요.
                5. 새 증분 노트의 내용을 기존 내용과 자연스럽게 통합하세요.
                6. 중복되거나 비슷한 설명은 하나로 정리하세요.
                7. knowledgePath에는 서비스의 대분류를 포함하지 마세요.
                8. knowledgePath는 실제 학습 주제를 넓은 개념부터 구체적인 개념 순서로 작성하세요.
                9. knowledgePath는 1개 이상 2개 이하의 항목으로 구성하세요.
                10. knowledgePath에는 WEB_DEVELOPMENT, COMPUTER_SCIENCE 같은 enum 대분류 이름을 작성하지 마세요.
                11. 기존 통합 노트의 knowledgePath와 의미가 크게 다르지 않다면 기존 경로를 유지하세요.
                12. 새 증분 노트가 기존 통합 노트의 세부 주제라면 새로운 상위 경로를 만들지 마세요.
                13. title은 전체 통합 내용을 대표하도록 작성하세요.
                14. description과 summary는 기존 내용과 신규 내용을 모두 반영하세요.
                15. keywords는 중복을 제거하고 3개 이상 7개 이하로 작성하세요.
                16. 결과는 한국어로 작성하되, 기술 고유명사는 일반적으로 사용하는 영문 표기를 유지하세요.
                
                knowledgePath 예시:
                
                Spring Batch 통합 노트:
                ["Spring Framework", "Spring Batch"]
                
                Spring WebFlux 통합 노트:
                ["Spring Framework", "Spring WebFlux"]
                
                Vue Composition API 통합 노트:
                ["Vue", "Composition API"]
                
                MySQL 인덱스 통합 노트:
                ["MySQL", "인덱스"]
                """;
    }

    public static String userPrompt(
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    ) {
        validateConsolidatedNote(
                consolidatedNote
        );

        validateIncrementalNote(
                incrementalNote
        );

        return """
                아래 통합 학습 노트와 새 증분 학습 노트를
                하나의 통합 노트로 병합하세요.
                
                <consolidated-note>
                제목:
                %s
                
                설명:
                %s
                
                요약:
                %s
                
                키워드:
                %s
                </consolidated-note>
                
                <incremental-note>
                제목:
                %s
                
                설명:
                %s
                
                요약:
                %s
                
                키워드:
                %s
                </incremental-note>
                """.formatted(
                consolidatedNote.getTitle(),
                consolidatedNote.getDescription(),
                consolidatedNote.getSummary(),
                String.join(
                        ", ",
                        consolidatedNote.getKeywords()
                ),
                incrementalNote.getTitle(),
                incrementalNote.getDescription(),
                incrementalNote.getSummary(),
                String.join(
                        ", ",
                        incrementalNote.getKeywords()
                )
        );
    }

    private static void validateConsolidatedNote(
            KnowledgeNote note
    ) {
        if (note == null) {
            throw new IllegalArgumentException(
                    "통합 KnowledgeNote는 필수입니다."
            );
        }

        if (!note.isConsolidated()) {
            throw new IllegalArgumentException(
                    "통합 노트 유형만 기존 노트로 사용할 수 있습니다."
            );
        }
    }

    private static void validateIncrementalNote(
            KnowledgeNote note
    ) {
        if (note == null) {
            throw new IllegalArgumentException(
                    "증분 KnowledgeNote는 필수입니다."
            );
        }

        if (!note.isIncremental()) {
            throw new IllegalArgumentException(
                    "증분 노트 유형만 신규 노트로 사용할 수 있습니다."
            );
        }
    }
}