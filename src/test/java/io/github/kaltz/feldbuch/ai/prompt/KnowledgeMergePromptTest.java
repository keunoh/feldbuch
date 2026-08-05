package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeMergePromptTest {
    private User user;
    private Conversation conversation;
    private Knowledge knowledge;

    private KnowledgeNote consolidatedNote;
    private KnowledgeNote incrementalNote;

    @BeforeEach
    void setUp() {
        user =
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

        conversation =
                Conversation.create(
                        user,
                        "Spring Batch 학습"
                );

        knowledge =
                Knowledge.createRoot(
                        user,
                        "웹 개발"
                );

        consolidatedNote =
                KnowledgeNote.createConsolidated(
                        user,
                        conversation,
                        knowledge,
                        "Spring Batch 종합 정리",
                        "Job과 Step 중심의 통합 노트",
                        "Spring Batch는 Job과 Step을 중심으로 작업을 구성합니다.",
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
                        knowledge,
                        "Tasklet과 Chunk 방식",
                        "Tasklet과 Chunk 차이를 정리한 증분 노트",
                        "Tasklet은 단일 작업을 수행하고 Chunk는 여러 Item을 묶어서 처리합니다.",
                        List.of(
                                "Tasklet",
                                "Chunk",
                                "Item"
                        )
                );
    }

    @Test
    void 시스템_프롬프트에_병합_규칙과_고정_대분류를_포함한다() {
        // when
        String result =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(result)
                .contains(
                        "기존 통합 학습 노트",
                        "새 증분 학습 노트",
                        "중복을 제거",
                        "\"rootCategory\"",
                        "\"knowledgePath\"",
                        "\"title\"",
                        "\"description\"",
                        "\"summary\"",
                        "\"keywords\"",
                        "WEB_DEVELOPMENT",
                        "웹 개발",
                        "DATABASE",
                        "데이터베이스"
                );

        assertThat(result)
                .contains(
                        "knowledgePath는 최대 2단계"
                );
    }

    @Test
    void 사용자_프롬프트에_통합_노트와_증분_노트의_전체_내용을_포함한다() {
        // when
        String result =
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(result)
                .contains(
                        "<consolidated-note>",
                        "</consolidated-note>",
                        "<incremental-note>",
                        "</incremental-note>"
                );

        assertThat(result)
                .contains(
                        consolidatedNote.getTitle(),
                        consolidatedNote.getDescription(),
                        consolidatedNote.getSummary(),
                        "Spring Batch, Job, Step"
                );

        assertThat(result)
                .contains(
                        incrementalNote.getTitle(),
                        incrementalNote.getDescription(),
                        incrementalNote.getSummary(),
                        "Tasklet, Chunk, Item"
                );
    }

    @Test
    void 통합_노트가_null이면_예외가_발생한다() {
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
    void 첫_번째_노트가_Incremental이면_예외가_발생한다() {
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
    void 증분_노트가_null이면_예외가_발생한다() {
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
    void 두_번째_노트가_Consolidated이면_예외가_발생한다() {
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