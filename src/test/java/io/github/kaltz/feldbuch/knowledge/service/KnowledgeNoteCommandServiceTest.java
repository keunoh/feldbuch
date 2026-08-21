package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNoteType;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeNoteCommandServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long CONVERSATION_ID = 10L;

    @Mock
    private KnowledgeCategoryResolver knowledgeCategoryResolver;

    @Mock
    private KnowledgeNoteRepository knowledgeNoteRepository;

    private KnowledgeNoteCommandService service;

    private User user;

    private Conversation conversation;

    private Knowledge springBatchKnowledge;

    private Knowledge jpaKnowledge;

    private AiKnowledgeSummaryResponse summaryResponse;

    private AiKnowledgeMergeResponse mergeResponse;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        service =
                new KnowledgeNoteCommandService(
                        knowledgeCategoryResolver,
                        knowledgeNoteRepository,
                        eventPublisher
                );

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
                USER_ID
        );

        conversation =
                Conversation.create(
                        user,
                        "Spring Batch 학습"
                );

        ReflectionTestUtils.setField(
                conversation,
                "id",
                CONVERSATION_ID
        );

        Knowledge webDevelopmentRoot =
                Knowledge.createRoot(
                        user,
                        "WEB_DEVELOPMENT"
                );

        ReflectionTestUtils.setField(
                webDevelopmentRoot,
                "id",
                100L
        );

        springBatchKnowledge =
                Knowledge.createChild(
                        user,
                        webDevelopmentRoot,
                        "Spring Batch"
                );

        ReflectionTestUtils.setField(
                springBatchKnowledge,
                "id",
                101L
        );

        Knowledge databaseRoot =
                Knowledge.createRoot(
                        user,
                        "DATABASE"
                );

        ReflectionTestUtils.setField(
                databaseRoot,
                "id",
                200L
        );

        jpaKnowledge =
                Knowledge.createChild(
                        user,
                        databaseRoot,
                        "JPA"
                );

        ReflectionTestUtils.setField(
                jpaKnowledge,
                "id",
                201L
        );

        summaryResponse =
                new AiKnowledgeSummaryResponse(
                        KnowledgeCategory.SPRING_BATCH,
                        "Spring Batch 기본 구조",
                        "Job과 Step 중심의 실행 구조를 정리한 노트",
                        "Spring Batch는 Job과 Step을 중심으로 배치 작업을 구성합니다.",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        mergeResponse =
                new AiKnowledgeMergeResponse(
                        KnowledgeCategory.SPRING_BATCH,
                        "Spring Batch 실행 구조와 처리 방식",
                        "Job, Step, Tasklet과 Chunk 구조를 정리한 통합 노트",
                        "Spring Batch는 Job과 Step으로 구성되며 Step은 Tasklet 또는 Chunk 방식으로 작업을 처리할 수 있습니다.",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step",
                                "Tasklet",
                                "Chunk"
                        )
                );
    }

    @Test
    void AI_요약으로_Incremental_노트를_저장한다() {
        // given
        when(
                knowledgeCategoryResolver.resolve(
                        user,
                        KnowledgeCategory.SPRING_BATCH
                )
        ).thenReturn(
                springBatchKnowledge
        );

        when(
                knowledgeNoteRepository.save(
                        any(KnowledgeNote.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        // when
        KnowledgeNote result =
                service.saveIncremental(
                        user,
                        conversation,
                        summaryResponse
                );

        // then
        ArgumentCaptor<KnowledgeNote> captor =
                ArgumentCaptor.forClass(
                        KnowledgeNote.class
                );

        verify(knowledgeCategoryResolver)
                .resolve(
                        user,
                        KnowledgeCategory.SPRING_BATCH
                );

        verify(knowledgeNoteRepository)
                .save(
                        captor.capture()
                );

        KnowledgeNote savedNote =
                captor.getValue();

        assertSummaryNote(
                savedNote,
                KnowledgeNoteType.INCREMENTAL
        );

        assertThat(savedNote.isIncremental())
                .isTrue();

        assertThat(savedNote.isConsolidated())
                .isFalse();

        assertThat(result)
                .isSameAs(savedNote);
    }

    @Test
    void AI_요약으로_Consolidated_노트를_저장한다() {
        // given
        when(
                knowledgeCategoryResolver.resolve(
                        user,
                        KnowledgeCategory.SPRING_BATCH
                )
        ).thenReturn(
                springBatchKnowledge
        );

        when(
                knowledgeNoteRepository.save(
                        any(KnowledgeNote.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        // when
        KnowledgeNote result =
                service.saveConsolidated(
                        user,
                        conversation,
                        summaryResponse
                );

        // then
        ArgumentCaptor<KnowledgeNote> captor =
                ArgumentCaptor.forClass(
                        KnowledgeNote.class
                );

        verify(knowledgeCategoryResolver)
                .resolve(
                        user,
                        KnowledgeCategory.SPRING_BATCH
                );

        verify(knowledgeNoteRepository)
                .save(
                        captor.capture()
                );

        KnowledgeNote savedNote =
                captor.getValue();

        assertSummaryNote(
                savedNote,
                KnowledgeNoteType.CONSOLIDATED
        );

        assertThat(savedNote.isConsolidated())
                .isTrue();

        assertThat(savedNote.isIncremental())
                .isFalse();

        assertThat(result)
                .isSameAs(savedNote);
    }

    @Test
    void AI_병합_결과로_Consolidated_노트의_내용을_갱신한다() {
        // given
        KnowledgeNote consolidatedNote =
                createConsolidatedNote(
                        springBatchKnowledge
                );

        when(
                knowledgeCategoryResolver.resolve(
                        user,
                        KnowledgeCategory.SPRING_BATCH
                )
        ).thenReturn(
                springBatchKnowledge
        );

        // when
        KnowledgeNote result =
                service.updateConsolidated(
                        user,
                        consolidatedNote,
                        mergeResponse
                );

        // then
        verify(knowledgeCategoryResolver)
                .resolve(
                        user,
                        KnowledgeCategory.SPRING_BATCH
                );

        verify(knowledgeNoteRepository, never())
                .save(
                        any(KnowledgeNote.class)
                );

        assertThat(result)
                .isSameAs(consolidatedNote);

        assertThat(consolidatedNote.getKnowledge())
                .isSameAs(springBatchKnowledge);

        assertMergeContent(
                consolidatedNote
        );
    }

    @Test
    void 병합_결과의_카테고리가_달라지면_통합_노트를_새_폴더로_이동한다() {
        // given
        KnowledgeNote consolidatedNote =
                createConsolidatedNote(
                        springBatchKnowledge
                );

        AiKnowledgeMergeResponse changedResponse =
                new AiKnowledgeMergeResponse(
                        KnowledgeCategory.JPA,
                        "영속성 컨텍스트와 변경 감지",
                        "JPA가 Entity를 관리하고 변경 사항을 반영하는 과정을 정리한 통합 노트",
                        "영속성 컨텍스트는 Entity를 관리하며 flush 시점에 변경 감지를 통해 SQL을 실행합니다.",
                        List.of(
                                "JPA",
                                "영속성 컨텍스트",
                                "flush",
                                "변경 감지"
                        )
                );

        when(
                knowledgeCategoryResolver.resolve(
                        user,
                        KnowledgeCategory.JPA
                )
        ).thenReturn(
                jpaKnowledge
        );

        // when
        KnowledgeNote result =
                service.updateConsolidated(
                        user,
                        consolidatedNote,
                        changedResponse
                );

        // then
        assertThat(result)
                .isSameAs(consolidatedNote);

        assertThat(consolidatedNote.getKnowledge())
                .isSameAs(jpaKnowledge);

        assertThat(consolidatedNote.getTitle())
                .isEqualTo(
                        changedResponse.title()
                );

        assertThat(consolidatedNote.getDescription())
                .isEqualTo(
                        changedResponse.description()
                );

        assertThat(consolidatedNote.getSummary())
                .isEqualTo(
                        changedResponse.summary()
                );

        assertThat(consolidatedNote.getKeywords())
                .containsExactlyElementsOf(
                        changedResponse.keywords()
                );
    }

    @Test
    void Incremental_노트는_통합_노트로_갱신할_수_없다() {
        // given
        KnowledgeNote incrementalNote =
                KnowledgeNote.createIncremental(
                        user,
                        conversation,
                        springBatchKnowledge,
                        "Tasklet과 Chunk",
                        "두 처리 방식의 차이를 정리한 노트",
                        "Tasklet은 단일 작업을 처리하고 Chunk는 Item을 묶어서 처리합니다.",
                        List.of(
                                "Spring Batch",
                                "Tasklet",
                                "Chunk"
                        )
                );

        // when & then
        assertThatThrownBy(() ->
                service.updateConsolidated(
                        user,
                        incrementalNote,
                        mergeResponse
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "통합 노트만 갱신할 수 있습니다."
                );

        verify(knowledgeCategoryResolver, never())
                .resolve(
                        any(),
                        any()
                );
    }

    @Test
    void AI_요약_응답의_카테고리가_null이면_저장에_실패한다() {
        // given
        AiKnowledgeSummaryResponse invalidResponse =
                new AiKnowledgeSummaryResponse(
                        null,
                        "제목",
                        "설명",
                        "요약",
                        List.of(
                                "키워드1",
                                "키워드2",
                                "키워드3"
                        )
                );

        // when & then
        assertThatThrownBy(() ->
                service.saveIncremental(
                        user,
                        conversation,
                        invalidResponse
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Knowledge 카테고리는 필수입니다."
                );

        verify(knowledgeCategoryResolver, never())
                .resolve(
                        any(),
                        any()
                );

        verify(knowledgeNoteRepository, never())
                .save(
                        any()
                );
    }

    @Test
    void AI_병합_응답의_카테고리가_null이면_갱신에_실패한다() {
        // given
        KnowledgeNote consolidatedNote =
                createConsolidatedNote(
                        springBatchKnowledge
                );

        AiKnowledgeMergeResponse invalidResponse =
                new AiKnowledgeMergeResponse(
                        null,
                        "제목",
                        "설명",
                        "요약",
                        List.of(
                                "키워드1",
                                "키워드2",
                                "키워드3"
                        )
                );

        // when & then
        assertThatThrownBy(() ->
                service.updateConsolidated(
                        user,
                        consolidatedNote,
                        invalidResponse
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Knowledge 카테고리는 필수입니다."
                );

        verify(knowledgeCategoryResolver, never())
                .resolve(
                        any(),
                        any()
                );
    }

    private KnowledgeNote createConsolidatedNote(
            Knowledge knowledge
    ) {
        KnowledgeNote note =
                KnowledgeNote.createConsolidated(
                        user,
                        conversation,
                        knowledge,
                        "기존 Spring Batch 노트",
                        "기존 통합 노트 설명",
                        "기존 통합 노트 요약",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        ReflectionTestUtils.setField(
                note,
                "id",
                300L
        );

        return note;
    }

    private void assertSummaryNote(
            KnowledgeNote note,
            KnowledgeNoteType expectedType
    ) {
        assertThat(note.getType())
                .isEqualTo(expectedType);

        assertThat(note.getUser())
                .isSameAs(user);

        assertThat(note.getConversation())
                .isSameAs(conversation);

        assertThat(note.getKnowledge())
                .isSameAs(springBatchKnowledge);

        assertThat(note.getTitle())
                .isEqualTo(
                        summaryResponse.title()
                );

        assertThat(note.getDescription())
                .isEqualTo(
                        summaryResponse.description()
                );

        assertThat(note.getSummary())
                .isEqualTo(
                        summaryResponse.summary()
                );

        assertThat(note.getKeywords())
                .containsExactlyElementsOf(
                        summaryResponse.keywords()
                );
    }

    private void assertMergeContent(
            KnowledgeNote note
    ) {
        assertThat(note.getType())
                .isEqualTo(
                        KnowledgeNoteType.CONSOLIDATED
                );

        assertThat(note.getTitle())
                .isEqualTo(
                        mergeResponse.title()
                );

        assertThat(note.getDescription())
                .isEqualTo(
                        mergeResponse.description()
                );

        assertThat(note.getSummary())
                .isEqualTo(
                        mergeResponse.summary()
                );

        assertThat(note.getKeywords())
                .containsExactlyElementsOf(
                        mergeResponse.keywords()
                );
    }
}