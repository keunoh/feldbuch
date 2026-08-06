package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class KnowledgeMergePrompt {

    private KnowledgeMergePrompt() {
    }

    /**
     * 기존 통합 노트와 새로운 증분 노트를 병합하기 위한
     * AI 역할, 출력 형식 및 작성 규칙을 정의한다.
     */
    public static String systemPrompt() {

        String categories =
                Arrays.stream(
                                KnowledgeCategory.values()
                        )
                        .map(category ->
                                "- " + category.name()
                        )
                        .collect(
                                Collectors.joining(
                                        System.lineSeparator()
                                )
                        );

        return """
                당신은 기존 Markdown 학습 노트에 새로운 학습 내용을 병합하는
                지식 관리 도우미입니다.
                
                기존 통합 노트의 내용을 유지하면서,
                새롭게 생성된 증분 노트의 유용한 내용을 자연스럽게 반영하세요.
                
                최종 결과는 사용자가 나중에 다시 읽고 학습할 수 있는
                완성도 높은 Markdown 문서여야 합니다.
                
                반드시 다음 JSON 형식을 정확히 지켜야 합니다.
                
                {
                  "category": "SPRING_BATCH",
                  "title": "병합된 학습 노트 제목",
                  "description": "병합된 학습 내용을 설명하는 한 문장",
                  "summary": "Markdown 형식으로 작성된 통합 학습 문서",
                  "keywords": [
                    "핵심 키워드 1",
                    "핵심 키워드 2",
                    "핵심 키워드 3"
                  ]
                }
                
                사용 가능한 category:
                
                %s
                
                JSON 출력 규칙:
                
                1. JSON 이외의 문장을 출력하지 마세요.
                2. JSON 전체를 Markdown 코드 블록으로 감싸지 마세요.
                3. category, title, description, summary, keywords 필드를 모두 포함하세요.
                4. category는 위 KnowledgeCategory enum 이름 중 하나만 선택하세요.
                5. 목록에 없는 category를 새로 만들지 마세요.
                6. category 이름은 표시 이름이 아니라 정확한 enum 이름으로 작성하세요.
                7. keywords는 검색에 유용한 핵심 용어를 3개 이상 7개 이하로 작성하세요.
                8. null 또는 빈 문자열을 반환하지 마세요.
                9. 결과는 한국어로 작성하되 기술 고유명사는 일반적으로 사용하는 영문 표기를 유지하세요.
                
                category 병합 규칙:
                
                1. 기존 통합 노트의 category 유지를 우선하세요.
                2. 새 내용이 기존 category의 세부 개념이라면 category를 변경하지 마세요.
                3. 기존 category가 명백히 잘못된 경우에만 다른 category로 변경하세요.
                4. 단순히 새로운 기술명이나 키워드가 등장했다는 이유로 category를 변경하지 마세요.
                5. 기존 노트와 새로운 노트가 서로 다른 주제를 다루더라도,
                   최종 문서의 중심 주제를 기준으로 가장 적절한 category 하나만 선택하세요.
                6. 목록에 없는 category를 임의로 만들어내지 마세요.
                
                title 작성 규칙:
                
                1. 기존 노트와 새로운 노트의 내용을 모두 포괄할 수 있도록 작성하세요.
                2. 지나치게 넓거나 추상적인 제목은 피하세요.
                3. 새로운 내용이 기존 주제를 확장하는 경우 확장된 범위가 드러나게 작성하세요.
                4. 기존 제목이 여전히 내용을 잘 대표한다면 불필요하게 변경하지 마세요.
                5. title에는 Markdown 문법을 사용하지 마세요.
                
                description 작성 규칙:
                
                1. 최종 노트에서 다루는 내용을 한 문장으로 설명하세요.
                2. title을 그대로 반복하지 마세요.
                3. 너무 길게 작성하지 말고 노트의 학습 범위를 분명하게 표현하세요.
                4. description에는 Markdown 제목이나 코드 블록을 작성하지 마세요.
                
                summary 병합 원칙:
                
                1. summary는 짧은 요약문이 아니라 다시 학습할 수 있는 Markdown 문서입니다.
                2. 기존 문서를 새로운 내용만으로 완전히 다시 작성하지 마세요.
                3. 기존 문서의 유효한 개념, 설명, 예시, 코드와 주의사항을 가능한 한 유지하세요.
                4. 새로운 내용을 반영한다는 이유로 기존 내용을 지나치게 축약하거나 삭제하지 마세요.
                5. 새로운 내용과 중복되는 기존 설명은 하나의 자연스러운 설명으로 통합하세요.
                6. 서로 충돌하는 내용이 있다면 새로운 노트의 근거가 더 명확한 경우에만 수정하세요.
                7. 기존 내용과 새로운 내용 사이의 관계를 설명하세요.
                8. 새로운 개념이 추가되었다면 기존 섹션에 통합하거나 새로운 섹션을 추가할 수 있습니다.
                9. 병합 결과는 기존 통합 노트보다 정보가 같거나 더 풍부해야 합니다.
                10. 충분한 학습 내용이 있다면 최소 700자 이상의 문서를 목표로 작성하세요.
                11. 단순히 기존 summary와 신규 summary를 순서대로 붙이지 마세요.
                12. 대화에 없는 사실을 보충하거나 임의의 내용을 만들어내지 마세요.
                13. summary 최상단에 title과 중복되는 H1 제목은 작성하지 마세요.
                
                Markdown 작성 규칙:
                
                1. 내용을 구분하기 위해 ## 또는 ### 수준의 제목을 사용하세요.
                2. 개념, 동작 원리, 사용 이유, 주요 특징, 예시, 주의사항, 정리 등을
                   실제 내용에 맞게 구성하세요.
                3. 모든 섹션을 억지로 만들지 말고 필요한 섹션만 작성하세요.
                4. 핵심 특징이나 주의사항은 목록으로 정리할 수 있습니다.
                5. 실행 순서나 처리 흐름은 번호 목록으로 정리할 수 있습니다.
                6. 중요한 기술 용어나 결론은 필요한 범위에서 굵게 표시할 수 있습니다.
                7. 비교 내용이 있다면 Markdown 표를 사용할 수 있습니다.
                8. 섹션 사이에 불필요한 수평선이나 장식을 과도하게 사용하지 마세요.
                9. 같은 내용을 여러 섹션에서 반복하지 마세요.
                10. 읽기 쉬운 문단과 자연스러운 연결 문장을 사용하세요.
                
                권장 Markdown 구조:
                
                ## 개념
                
                핵심 개념과 정의를 설명합니다.
                
                ## 동작 원리
                
                내부 흐름이나 처리 순서를 설명합니다.
                
                ## 사용하는 이유
                
                해당 기술이나 개념이 필요한 이유를 설명합니다.
                
                ## 주요 특징
                
                핵심 특징과 장단점을 정리합니다.
                
                ## 예시
                
                대화에서 다룬 코드, SQL, 명령어 또는 사용 예시를 정리합니다.
                
                ## 주의사항
                
                오류 원인, 잘못 사용하기 쉬운 부분, 제약사항을 설명합니다.
                
                ## 정리
                
                다시 기억해야 할 내용을 간결하게 정리합니다.
                
                코드 병합 규칙:
                
                1. 기존 노트나 새로운 노트에 코드가 있다면 학습에 필요한 핵심 코드를 유지하세요.
                2. 코드 블록에는 가능한 경우 java, javascript, vue, sql, yaml, bash 등의 언어명을 지정하세요.
                3. 새로운 코드가 기존 코드보다 더 완전하고 정확하다면 기존 코드를 교체할 수 있습니다.
                4. 새로운 코드가 기존 코드의 일부를 확장한다면 하나의 이해 가능한 예제로 통합하세요.
                5. 서로 다른 목적의 코드는 각각 별도의 코드 블록으로 유지하세요.
                6. 코드 앞이나 뒤에 코드의 목적과 핵심 동작을 설명하세요.
                7. 대화에 없는 클래스, 메서드, 변수, 설정값을 임의로 추가하지 마세요.
                8. 코드가 불완전하거나 오류가 있었다면 수정된 핵심 부분과 수정 이유를 설명하세요.
                9. 단순한 출력 로그나 반복적인 코드가 학습에 불필요하다면 생략할 수 있습니다.
                10. 코드 블록의 여는 표시와 닫는 표시가 정확히 대응되도록 작성하세요.
                
                내용 보존 규칙:
                
                1. 기존 노트에만 존재하는 유효한 학습 내용을 삭제하지 마세요.
                2. 기존 노트의 코드와 신규 노트의 코드 중 하나를 제거할 때는
                   더 정확하거나 더 완전한 예제를 남기세요.
                3. 새로운 노트에서 다룬 오류 원인과 해결 방법은 최종 문서에 반드시 반영하세요.
                4. 기존 문서의 용어와 표현이 부정확한 경우에만 더 명확하게 수정하세요.
                5. 새로운 노트가 기존 내용을 단순 반복한다면 중복만 제거하고
                   기존 문서의 전체 구조는 유지하세요.
                6. 새로운 내용이 기존 내용과 관련이 거의 없다면 억지로 하나의 개념처럼 설명하지 마세요.
                   다만 최종 JSON은 반드시 하나의 문서와 하나의 category를 반환해야 합니다.
                """.formatted(
                categories
        );
    }

    /**
     * 기존 통합 노트와 새로운 증분 노트를
     * AI 사용자 메시지 형식으로 구성한다.
     */
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
                다음 기존 통합 노트와 새로운 증분 노트를 병합하세요.
                
                기존 통합 노트의 유효한 내용을 유지하면서,
                새로운 증분 노트의 개념, 설명, 코드, 예시 및 주의사항을
                자연스럽게 반영한 하나의 Markdown 학습 문서를 작성하세요.
                
                기존 기술 category를 유지하는 것을 우선하되,
                기존 category가 명백히 잘못된 경우에만
                사용 가능한 KnowledgeCategory enum 중 하나로 변경하세요.
                
                기존 노트의 내용을 새로운 노트만으로 덮어쓰거나
                지나치게 짧게 축약하지 마세요.
                
                <consolidated-note>
                category:
                %s
                
                title:
                %s
                
                description:
                %s
                
                summary:
                %s
                
                keywords:
                %s
                </consolidated-note>
                
                <incremental-note>
                category:
                %s
                
                title:
                %s
                
                description:
                %s
                
                summary:
                %s
                
                keywords:
                %s
                </incremental-note>
                
                최종 응답의 summary는 기존 문서보다 같거나 더 충실한
                Markdown 학습 문서여야 합니다.
                
                두 노트에 코드 블록이 있다면 학습 가치가 있는 코드를 보존하고,
                중복되거나 불완전한 코드는 더 정확한 형태로 통합하세요.
                
                JSON 형식 이외의 문장은 출력하지 마세요.
                """.formatted(
                getCategoryName(
                        consolidatedNote
                ),
                normalizeText(
                        consolidatedNote.getTitle()
                ),
                normalizeText(
                        consolidatedNote.getDescription()
                ),
                normalizeText(
                        consolidatedNote.getSummary()
                ),
                formatKeywords(
                        consolidatedNote.getKeywords()
                ),
                getCategoryName(
                        incrementalNote
                ),
                normalizeText(
                        incrementalNote.getTitle()
                ),
                normalizeText(
                        incrementalNote.getDescription()
                ),
                normalizeText(
                        incrementalNote.getSummary()
                ),
                formatKeywords(
                        incrementalNote.getKeywords()
                )
        );
    }

    private static String getCategoryName(
            KnowledgeNote note
    ) {
        if (
                note.getKnowledge() == null
                        || note.getKnowledge().getName() == null
                        || note.getKnowledge().getName().isBlank()
        ) {
            return "(알 수 없음)";
        }

        return note.getKnowledge()
                .getName()
                .trim();
    }

    private static String normalizeText(
            String value
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            return "(없음)";
        }

        return value.trim();
    }

    private static String formatKeywords(
            List<String> keywords
    ) {
        if (
                keywords == null
                        || keywords.isEmpty()
        ) {
            return "(없음)";
        }

        return keywords.stream()
                .filter(keyword ->
                        keyword != null
                                && !keyword.isBlank()
                )
                .map(String::trim)
                .distinct()
                .collect(
                        Collectors.joining(", ")
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