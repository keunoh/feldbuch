package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeMergePromptTest {

    private KnowledgeNote consolidatedNote;

    private KnowledgeNote incrementalNote;

    @BeforeEach
    void setUp() {
        User user =
                User.builder()
                        .email("test@test.com")
                        .password("password")
                        .nickname("tester")
                        .role(UserRole.USER)
                        .build();

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        Conversation conversation =
                Conversation.create(
                        user,
                        "Spring Batch 학습"
                );

        ReflectionTestUtils.setField(
                conversation,
                "id",
                10L
        );

        Knowledge root =
                Knowledge.createRoot(
                        user,
                        "WEB_DEVELOPMENT"
                );

        ReflectionTestUtils.setField(
                root,
                "id",
                100L
        );

        Knowledge springBatch =
                Knowledge.createChild(
                        user,
                        root,
                        "Spring Batch"
                );

        ReflectionTestUtils.setField(
                springBatch,
                "id",
                101L
        );

        consolidatedNote =
                KnowledgeNote.createConsolidated(
                        user,
                        conversation,
                        springBatch,
                        "Spring Batch 기본 구조",
                        "Job과 Step을 중심으로 정리한 통합 노트",
                        "Spring Batch는 Job과 Step을 중심으로 배치 작업을 구성합니다.",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        incrementalNote =
                KnowledgeNote.createIncremental(
                        user,
                        conversation,
                        springBatch,
                        "Tasklet과 Chunk 처리 방식",
                        "두 Step 처리 방식의 차이를 정리한 증분 노트",
                        "Tasklet은 단일 작업을 수행하고 Chunk는 여러 Item을 일정 단위로 처리합니다.",
                        List.of(
                                "Spring Batch",
                                "Tasklet",
                                "Chunk"
                        )
                );
    }

    @Test
    @DisplayName("System Prompt는 category 기반 JSON 형식을 안내한다.")
    void systemPromptContainsCategoryJsonFormat() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "\"category\"",
                        "\"title\"",
                        "\"description\"",
                        "\"summary\"",
                        "\"keywords\""
                );

        assertThat(prompt)
                .doesNotContain(
                        "\"knowledgePath\""
                );
    }

    @Test
    @DisplayName("System Prompt에는 모든 KnowledgeCategory enum이 포함된다.")
    void systemPromptContainsAllKnowledgeCategories() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        for (
                KnowledgeCategory category
                : KnowledgeCategory.values()
        ) {
            assertThat(prompt)
                    .contains(
                            category.name()
                    );
        }
    }

    @Test
    @DisplayName("System Prompt는 기존 category 유지를 우선하도록 안내한다.")
    void systemPromptGuidesCategoryStability() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "기존 category를 유지",
                        "세부 개념이라면 category를 변경하지 마세요",
                        "기존 category가 명백히 잘못된 경우에만"
                );
    }

    @Test
    @DisplayName("System Prompt는 고정 category만 선택하도록 안내한다.")
    void systemPromptRejectsArbitraryCategories() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "하나만 선택",
                        "목록에 없는 category를 새로 만들지 마세요",
                        "enum 이름"
                );
    }

    @Test
    @DisplayName("User Prompt는 통합 노트와 증분 노트 전체 내용을 포함한다.")
    void userPromptContainsBothNotes() {
        // when
        String prompt =
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(prompt)
                .contains(
                        "<consolidated-note>",
                        "</consolidated-note>",
                        "<incremental-note>",
                        "</incremental-note>"
                );

        assertThat(prompt)
                .contains(
                        consolidatedNote.getTitle(),
                        consolidatedNote.getDescription(),
                        consolidatedNote.getSummary(),
                        incrementalNote.getTitle(),
                        incrementalNote.getDescription(),
                        incrementalNote.getSummary()
                );
    }

    @Test
    @DisplayName("User Prompt는 두 노트의 키워드를 포함한다.")
    void userPromptContainsKeywords() {
        // when
        String prompt =
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(prompt)
                .contains(
                        "Spring Batch, Job, Step",
                        "Spring Batch, Tasklet, Chunk"
                );
    }

    @Test
    @DisplayName("User Prompt는 고정 KnowledgeCategory 중 하나를 선택하도록 안내한다.")
    void userPromptGuidesFixedCategorySelection() {
        // when
        String prompt =
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(prompt)
                .contains(
                        "KnowledgeCategory enum 중 하나만 선택",
                        "기존 기술 category를 유지"
                );
    }

    @Test
    @DisplayName("통합 노트가 null이면 예외가 발생한다.")
    void consolidatedNoteIsNull() {
        assertThatThrownBy(() ->
                KnowledgeMergePrompt.userPrompt(
                        null,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "통합 KnowledgeNote는 필수입니다."
                );
    }

    @Test
    @DisplayName("첫 번째 노트가 Incremental이면 예외가 발생한다.")
    void firstNoteMustBeConsolidated() {
        assertThatThrownBy(() ->
                KnowledgeMergePrompt.userPrompt(
                        incrementalNote,
                        incrementalNote
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "통합 노트 유형만 기존 노트로 사용할 수 있습니다."
                );
    }

    @Test
    @DisplayName("증분 노트가 null이면 예외가 발생한다.")
    void incrementalNoteIsNull() {
        assertThatThrownBy(() ->
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "증분 KnowledgeNote는 필수입니다."
                );
    }

    @Test
    @DisplayName("두 번째 노트가 Consolidated이면 예외가 발생한다.")
    void secondNoteMustBeIncremental() {
        assertThatThrownBy(() ->
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        consolidatedNote
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "증분 노트 유형만 신규 노트로 사용할 수 있습니다."
                );
    }
}