package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeSummaryPromptTest {

    @Test
    @DisplayName("System Prompt는 category 기반 JSON 형식을 안내한다.")
    void systemPrompt() {

        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains("\"category\"");

        assertThat(prompt)
                .contains("\"title\"");

        assertThat(prompt)
                .contains("\"description\"");

        assertThat(prompt)
                .contains("\"summary\"");

        assertThat(prompt)
                .contains("\"keywords\"");
    }

    @Test
    @DisplayName("System Prompt에는 모든 KnowledgeCategory enum이 포함된다.")
    void containsAllCategories() {

        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        for (KnowledgeCategory category : KnowledgeCategory.values()) {
            assertThat(prompt)
                    .contains(category.name());
        }
    }

    @Test
    @DisplayName("knowledgePath라는 단어는 포함하지 않는다.")
    void noKnowledgePath() {

        // when
        String prompt =
                KnowledgeSummaryPrompt.systemPrompt();

        // then
        assertThat(prompt)
                .doesNotContain("knowledgePath");
    }

    @Test
    @DisplayName("userPrompt는 conversation을 포함한다.")
    void userPrompt() {

        // given
        String conversation = """
                USER:
                Spring Batch 알려줘.
                
                AI:
                Job과 Step 구조입니다.
                """;

        // when
        String prompt =
                KnowledgeSummaryPrompt.userPrompt(
                        conversation
                );

        // then
        assertThat(prompt)
                .contains("<conversation>");

        assertThat(prompt)
                .contains("</conversation>");

        assertThat(prompt)
                .contains(conversation);
    }

    @Test
    @DisplayName("공백 대화는 예외가 발생한다.")
    void blankConversation() {

        assertThatThrownBy(() ->
                KnowledgeSummaryPrompt.userPrompt(" ")
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage("요약할 대화 내용은 필수입니다.");
    }

    @Test
    @DisplayName("null 대화는 예외가 발생한다.")
    void nullConversation() {

        assertThatThrownBy(() ->
                KnowledgeSummaryPrompt.userPrompt(null)
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage("요약할 대화 내용은 필수입니다.");
    }
}