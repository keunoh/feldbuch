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
                        existingMarkdownSummary(),
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
                        incrementalMarkdownSummary(),
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
                )
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
                            "- " + category.name()
                    );
        }
    }

    @Test
    @DisplayName("System Prompt는 JSON 이외의 출력을 금지한다.")
    void systemPromptRestrictsOutputToJson() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "JSON 이외의 문장을 출력하지 마세요",
                        "JSON 전체를 Markdown 코드 블록으로 감싸지 마세요",
                        "category, title, description, summary, keywords 필드를 모두 포함하세요"
                );
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
                        "기존 통합 노트의 category 유지를 우선하세요",
                        "새 내용이 기존 category의 세부 개념이라면 category를 변경하지 마세요",
                        "기존 category가 명백히 잘못된 경우에만 다른 category로 변경하세요"
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
                        "KnowledgeCategory enum 이름 중 하나만 선택하세요",
                        "목록에 없는 category를 새로 만들지 마세요",
                        "정확한 enum 이름으로 작성하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 단순한 새 키워드로 category를 변경하지 않도록 안내한다.")
    void systemPromptRejectsUnnecessaryCategoryChange() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "단순히 새로운 기술명이나 키워드가 등장했다는 이유로 category를 변경하지 마세요",
                        "최종 문서의 중심 주제를 기준으로 가장 적절한 category 하나만 선택하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 기존 문서를 보존하며 병합하도록 안내한다.")
    void systemPromptGuidesExistingContentPreservation() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "기존 문서를 새로운 내용만으로 완전히 다시 작성하지 마세요",
                        "기존 문서의 유효한 개념, 설명, 예시, 코드와 주의사항을 가능한 한 유지하세요",
                        "기존 내용을 지나치게 축약하거나 삭제하지 마세요",
                        "병합 결과는 기존 통합 노트보다 정보가 같거나 더 풍부해야 합니다"
                );
    }

    @Test
    @DisplayName("System Prompt는 단순 이어 붙이기가 아닌 자연스러운 통합을 안내한다.")
    void systemPromptGuidesNaturalMerge() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "새로운 내용과 중복되는 기존 설명은 하나의 자연스러운 설명으로 통합하세요",
                        "기존 내용과 새로운 내용 사이의 관계를 설명하세요",
                        "단순히 기존 summary와 신규 summary를 순서대로 붙이지 마세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 충분한 통합 문서 길이를 요청한다.")
    void systemPromptGuidesMinimumDocumentLength() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "최소 700자 이상의 문서를 목표로 작성하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 Markdown 소제목 구조를 안내한다.")
    void systemPromptContainsMarkdownSections() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "## 개념",
                        "## 동작 원리",
                        "## 사용하는 이유",
                        "## 주요 특징",
                        "## 예시",
                        "## 주의사항",
                        "## 정리"
                );
    }

    @Test
    @DisplayName("System Prompt는 필요한 Markdown 섹션만 작성하도록 안내한다.")
    void systemPromptDoesNotForceAllSections() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "모든 섹션을 억지로 만들지 말고 필요한 섹션만 작성하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 목록과 표를 활용하도록 안내한다.")
    void systemPromptGuidesMarkdownListsAndTables() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "핵심 특징이나 주의사항은 목록으로 정리할 수 있습니다",
                        "실행 순서나 처리 흐름은 번호 목록으로 정리할 수 있습니다",
                        "비교 내용이 있다면 Markdown 표를 사용할 수 있습니다"
                );
    }

    @Test
    @DisplayName("System Prompt는 제목과 중복되는 H1을 금지한다.")
    void systemPromptRejectsDuplicatedH1() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "summary 최상단에 title과 중복되는 H1 제목은 작성하지 마세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 기존 및 신규 코드의 보존과 통합을 안내한다.")
    void systemPromptGuidesCodePreservationAndMerge() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "기존 노트나 새로운 노트에 코드가 있다면 학습에 필요한 핵심 코드를 유지하세요",
                        "새로운 코드가 기존 코드보다 더 완전하고 정확하다면 기존 코드를 교체할 수 있습니다",
                        "새로운 코드가 기존 코드의 일부를 확장한다면 하나의 이해 가능한 예제로 통합하세요",
                        "서로 다른 목적의 코드는 각각 별도의 코드 블록으로 유지하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 코드 블록 언어와 설명을 안내한다.")
    void systemPromptGuidesCodeBlockLanguageAndExplanation() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "java, javascript, vue, sql, yaml, bash",
                        "코드 앞이나 뒤에 코드의 목적과 핵심 동작을 설명하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 대화에 없는 코드를 만들지 않도록 안내한다.")
    void systemPromptRejectsInventedCode() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "대화에 없는 클래스, 메서드, 변수, 설정값을 임의로 추가하지 마세요",
                        "대화에 없는 사실을 보충하거나 임의의 내용을 만들어내지 마세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 코드 오류의 수정 이유를 설명하도록 안내한다.")
    void systemPromptGuidesCodeCorrectionExplanation() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "코드가 불완전하거나 오류가 있었다면 수정된 핵심 부분과 수정 이유를 설명하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 코드 블록을 정상적으로 닫도록 안내한다.")
    void systemPromptGuidesClosedCodeFence() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "코드 블록의 여는 표시와 닫는 표시가 정확히 대응되도록 작성하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 오류 원인과 해결 방법을 보존하도록 안내한다.")
    void systemPromptGuidesErrorAndSolutionPreservation() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "새로운 노트에서 다룬 오류 원인과 해결 방법은 최종 문서에 반드시 반영하세요"
                );
    }

    @Test
    @DisplayName("System Prompt는 키워드 개수 규칙을 안내한다.")
    void systemPromptContainsKeywordCountRule() {
        // when
        String prompt =
                KnowledgeMergePrompt.systemPrompt();

        // then
        assertThat(prompt)
                .contains(
                        "keywords는 검색에 유용한 핵심 용어를 3개 이상 7개 이하로 작성하세요"
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
    @DisplayName("User Prompt는 두 노트의 category를 포함한다.")
    void userPromptContainsCategories() {
        // when
        String prompt =
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(prompt)
                .contains(
                        "category:",
                        consolidatedNote
                                .getKnowledge()
                                .getName(),
                        incrementalNote
                                .getKnowledge()
                                .getName()
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
                        "기존 기술 category를 유지하는 것을 우선하되",
                        "사용 가능한 KnowledgeCategory enum 중 하나로 변경하세요"
                );
    }

    @Test
    @DisplayName("User Prompt는 기존 문서를 덮어쓰거나 축약하지 않도록 안내한다.")
    void userPromptGuidesExistingDocumentPreservation() {
        // when
        String prompt =
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(prompt)
                .contains(
                        "기존 노트의 내용을 새로운 노트만으로 덮어쓰거나",
                        "지나치게 짧게 축약하지 마세요",
                        "기존 문서보다 같거나 더 충실한"
                );
    }

    @Test
    @DisplayName("User Prompt는 두 노트의 코드를 보존하고 통합하도록 안내한다.")
    void userPromptGuidesCodeMerge() {
        // when
        String prompt =
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(prompt)
                .contains(
                        "두 노트에 코드 블록이 있다면 학습 가치가 있는 코드를 보존하고",
                        "중복되거나 불완전한 코드는 더 정확한 형태로 통합하세요"
                );
    }

    @Test
    @DisplayName("User Prompt는 JSON 형식 외 출력을 금지한다.")
    void userPromptRestrictsOutputToJson() {
        // when
        String prompt =
                KnowledgeMergePrompt.userPrompt(
                        consolidatedNote,
                        incrementalNote
                );

        // then
        assertThat(prompt)
                .contains(
                        "JSON 형식 이외의 문장은 출력하지 마세요"
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

    private String existingMarkdownSummary() {
        return """
                ## 개념
                
                Spring Batch는 Job과 Step을 중심으로 배치 작업을 구성합니다.
                Job은 전체 배치 실행 단위이며 Step은 실제 처리 로직을 담당합니다.
                
                ## 동작 원리
                
                Job이 시작되면 등록된 Step이 정의된 순서에 따라 실행됩니다.
                
                ## 예시
                
                ```java
                return new JobBuilder(
                        "sampleJob",
                        jobRepository
                )
                        .start(sampleStep)
                        .build();
                ```
                
                ## 정리
                
                Job은 전체 흐름을 담당하고 Step은 실제 작업을 수행합니다.
                """;
    }

    private String incrementalMarkdownSummary() {
        return """
                ## Tasklet 방식
                
                Tasklet은 하나의 명확한 작업을 직접 실행하는 방식입니다.
                
                ## Chunk 방식
                
                Chunk는 여러 Item을 일정 단위로 읽고 처리한 뒤 저장합니다.
                
                ## 예시
                
                ```java
                return new StepBuilder(
                        "chunkStep",
                        jobRepository
                )
                        .<String, String>chunk(
                                100,
                                transactionManager
                        )
                        .reader(reader)
                        .writer(writer)
                        .build();
                ```
                
                ## 정리
                
                단순 작업에는 Tasklet이 적합하고 대량 데이터 처리에는 Chunk가 적합합니다.
                """;
    }
}