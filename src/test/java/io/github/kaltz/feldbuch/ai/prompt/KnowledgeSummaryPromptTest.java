package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeSummaryPromptTest {

    @Test
    @DisplayName("시스템 프롬프트는 고정된 JSON 응답 형식을 안내한다.")
    void systemPromptContainsJsonResponseFormat() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "\"category\"",
                        "\"title\"",
                        "\"description\"",
                        "\"summary\"",
                        "\"keywords\""
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 knowledgePath를 사용하지 않는다.")
    void systemPromptDoesNotContainKnowledgePath() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .doesNotContain(
                        "\"knowledgePath\""
                );
    }

    @Test
    @DisplayName("시스템 프롬프트에는 모든 KnowledgeCategory가 포함된다.")
    void systemPromptContainsAllKnowledgeCategories() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        for (
                KnowledgeCategory category
                : KnowledgeCategory.values()
        ) {
            assertThat(prompt)
                    .contains(
                            "- " + category.name()
                    );
        }
    }

    @Test
    @DisplayName("시스템 프롬프트는 고정된 enum 카테고리만 선택하도록 안내한다.")
    void systemPromptGuidesFixedCategorySelection() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "KnowledgeCategory enum 이름 중 하나만 사용하세요",
                        "목록에 없는 category를 새로 만들지 마세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 JSON 이외의 출력을 금지한다.")
    void systemPromptRestrictsOutputToJson() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "JSON 이외의 문장을 출력하지 마세요",
                        "JSON 전체를 Markdown 코드 블록으로 감싸지 마세요",
                        "모든 필드를 반드시 포함하세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 summary를 Markdown 학습 문서로 작성하도록 안내한다.")
    void systemPromptGuidesMarkdownLearningDocument() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "summary는 짧은 요약문이 아니라 다시 학습할 수 있는 Markdown 문서로 작성하세요",
                        "최소 500자 이상을 목표로 작성하세요",
                        "원문을 그대로 복사하지 말고",
                        "학습자가 다시 이해하기 쉬운 구조로 재구성하세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 Markdown 소제목 구조를 안내한다.")
    void systemPromptContainsRecommendedMarkdownSections() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "## 개념",
                        "## 동작 원리",
                        "## 사용하는 이유",
                        "## 주요 특징",
                        "## 예시",
                        "## 정리"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 대화에 맞지 않는 섹션을 강제하지 않는다.")
    void systemPromptDoesNotForceIrrelevantSections() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "대화 내용에 맞지 않는 섹션은 억지로 만들지 마세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 목록과 표를 활용하도록 안내한다.")
    void systemPromptGuidesListsAndTables() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "문단, 목록, 소제목을 활용",
                        "Markdown 표",
                        "번호 목록",
                        "굵게 표시"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 제목과 중복되는 H1을 작성하지 않도록 안내한다.")
    void systemPromptRejectsDuplicatedH1Title() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "summary 최상단에 title과 중복되는 H1 제목은 작성하지 마세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 대화에 코드가 있을 때 코드 블록으로 보존하도록 안내한다.")
    void systemPromptGuidesCodeBlockPreservation() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "대화에 코드가 포함되어 있다면",
                        "Markdown 코드 블록으로 포함하세요",
                        "java, javascript, vue, sql, yaml, bash",
                        "핵심 부분을 선별하세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 코드의 목적과 동작을 설명하도록 안내한다.")
    void systemPromptGuidesCodeExplanation() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "코드의 동작을 설명하는 문장",
                        "코드 블록 앞이나 뒤에 작성하세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 대화에 없는 코드를 만들지 않도록 안내한다.")
    void systemPromptRejectsInventedCode() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "대화에 코드가 없었다면 새로운 코드를 억지로 만들지 마세요",
                        "대화에 없는 클래스명, 메서드명, 설정값을 임의로 만들어내지 마세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 오류가 있는 코드의 수정 이유를 설명하도록 안내한다.")
    void systemPromptGuidesCorrectedCodeExplanation() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "코드가 불완전하거나 오류가 있었다면",
                        "수정된 부분과 이유를 함께 설명하세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 개념과 동작 원리 및 사용 이유를 포함하도록 안내한다.")
    void systemPromptGuidesRichLearningContent() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "개념 사이의 관계를 설명하세요",
                        "왜 사용하는지",
                        "어떻게 동작하는지",
                        "주의점, 오류 원인, 해결 방법"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 부족한 내용을 임의로 부풀리지 않도록 안내한다.")
    void systemPromptRejectsUnsupportedExpansion() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "충분한 내용이 없는 대화라면",
                        "내용을 부풀리거나 사실을 만들어내지 마세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 키워드 개수 규칙을 안내한다.")
    void systemPromptContainsKeywordCountRule() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "keywords는 검색에 유용한 핵심 용어를 3개 이상 7개 이하로 작성하세요"
                );
    }

    @Test
    @DisplayName("시스템 프롬프트는 한국어와 기술 고유명사 표기 규칙을 안내한다.")
    void systemPromptContainsLanguageRule() {
        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "결과는 한국어로 작성하되",
                        "기술 고유명사는 일반적으로 사용하는 영문 표기를 유지하세요"
                );
    }

    @Test
    @DisplayName("사용자 프롬프트는 전달받은 대화를 포함한다.")
    void userPromptContainsConversation() {
        // given
        String conversation = """
                USER:
                Spring Batch의 Job과 Step 차이를 설명해줘.
                
                ASSISTANT:
                Job은 전체 배치 실행 단위이고,
                Step은 실제 작업을 수행하는 단위입니다.
                """;

        // when
        String prompt =
                KnowledgeSummaryPrompt.userPrompt(
                        conversation
                );

        // then
        assertThat(prompt)
                .contains(
                        "<conversation>",
                        "</conversation>",
                        conversation.trim()
                );
    }

    @Test
    @DisplayName("사용자 프롬프트는 Markdown 학습 노트 생성을 요청한다.")
    void userPromptRequestsMarkdownLearningNote() {
        // given
        String conversation =
                "Spring Batch를 설명해줘.";

        // when
        String prompt =
                KnowledgeSummaryPrompt.userPrompt(
                        conversation
                );

        // then
        assertThat(prompt)
                .contains(
                        "다시 학습할 수 있는 Markdown 지식 노트로 정리하세요"
                );
    }

    @Test
    @DisplayName("사용자 프롬프트는 코드와 SQL 및 설정 예시를 보존하도록 안내한다.")
    void userPromptGuidesTechnicalExamplePreservation() {
        // given
        String conversation = """
                다음 Spring Batch 설정을 설명해줘.
                
                ```java
                return new JobBuilder(
                        "sampleJob",
                        jobRepository
                )
                        .start(sampleStep)
                        .build();
                ```
                """;

        // when
        String prompt =
                KnowledgeSummaryPrompt.userPrompt(
                        conversation
                );

        // then
        assertThat(prompt)
                .contains(
                        "코드, SQL, 명령어 또는 설정 예시가 포함되어 있다면",
                        "summary의 Markdown 코드 블록에 포함하세요",
                        "JobBuilder",
                        "sampleStep"
                );
    }

    @Test
    @DisplayName("사용자 프롬프트는 대화에 없는 사실과 코드를 만들지 않도록 안내한다.")
    void userPromptRejectsInventedFactsAndCode() {
        // given
        String conversation =
                "Spring Batch의 Job을 설명해줘.";

        // when
        String prompt =
                KnowledgeSummaryPrompt.userPrompt(
                        conversation
                );

        // then
        assertThat(prompt)
                .contains(
                        "대화에 없는 사실이나 코드를 새로 만들어내지 마세요"
                );
    }

    @Test
    @DisplayName("사용자 프롬프트는 대화 앞뒤 공백을 제거한다.")
    void userPromptTrimsConversation() {
        // given
        String conversation =
                "   Spring Batch의 Job을 설명해줘.   ";

        // when
        String prompt =
                KnowledgeSummaryPrompt.userPrompt(
                        conversation
                );

        // then
        assertThat(prompt)
                .contains(
                        """
                                <conversation>
                                Spring Batch의 Job을 설명해줘.
                                </conversation>
                                """
                )
                .doesNotContain(
                        """
                                <conversation>
                                   Spring Batch
                                """
                );
    }

    @Test
    @DisplayName("대화가 null이면 예외가 발생한다.")
    void conversationIsNull() {
        assertThatThrownBy(() ->
                KnowledgeSummaryPrompt.userPrompt(
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "요약할 대화 내용은 필수입니다."
                );
    }

    @Test
    @DisplayName("대화가 빈 문자열이면 예외가 발생한다.")
    void conversationIsEmpty() {
        assertThatThrownBy(() ->
                KnowledgeSummaryPrompt.userPrompt(
                        ""
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "요약할 대화 내용은 필수입니다."
                );
    }

    @Test
    @DisplayName("대화가 공백이면 예외가 발생한다.")
    void conversationIsBlank() {
        assertThatThrownBy(() ->
                KnowledgeSummaryPrompt.userPrompt(
                        "   "
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "요약할 대화 내용은 필수입니다."
                );
    }
}