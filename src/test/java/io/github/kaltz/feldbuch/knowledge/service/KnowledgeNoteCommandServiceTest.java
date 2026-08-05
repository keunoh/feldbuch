package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeNoteCommandServiceTest {

    @Mock
    private KnowledgePathResolver knowledgePathResolver;

    @Mock
    private KnowledgeNoteRepository knowledgeNoteRepository;

    private KnowledgeNoteCommandService service;

    private User user;
    private Conversation conversation;

    private Knowledge currentKnowledge;
    private Knowledge resolvedKnowledge;

    private AiKnowledgeSummaryResponse summaryResponse;
    private AiKnowledgeMergeResponse mergeResponse;

    @BeforeEach
    void setUp() {
        service =
                new KnowledgeNoteCommandService(
                        knowledgePathResolver,
                        knowledgeNoteRepository
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
                1L
        );

        conversation =
                Conversation.create(
                        user,
                        "Spring Batch 학습"
                );

        ReflectionTestUtils.setField(
                conversation,
                "id",
                10L
        );

        currentKnowledge =
                Knowledge.createRoot(
                        user,
                        "웹 개발"
                );

        ReflectionTestUtils.setField(
                currentKnowledge,
                "id",
                100L
        );

        resolvedKnowledge =
                Knowledge.createChild(
                        user,
                        currentKnowledge,
                        "Spring Batch"
                );

        ReflectionTestUtils.setField(
                resolvedKnowledge,
                "id",
                101L
        );

        summaryResponse =
                new AiKnowledgeSummaryResponse(
                        List.of(
                                "Spring Framework",
                                "Spring Batch"
                        ),
                        "Spring Batch 기본 구조",
                        "Job과 Step 중심의 실행 구조를 정리한 노트",
                        "Spring Batch는 Job과 Step을 중심으로 대용량 일괄 처리를 구성합니다.",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        mergeResponse =
                new AiKnowledgeMergeResponse(
                        List.of(
                                "Spring Framework",
                                "Spring Batch"
                        ),
                        "Spring Batch 처리 방식",
                        "Tasklet과 Chunk 방식까지 반영한 통합 노트",
                        "Spring Batch는 Job과 Step으로 구성되며 Tasklet 또는 Chunk 방식으로 작업을 처리할 수 있습니다.",
                        List.of(
                                "Spring Batch",
                                "Tasklet",
                                "Chunk"
                        )
                );
    }

    @Test
    void AI_요약으로_Incremental_노트를_저장한다() {
        // given
        mockResolvedKnowledge();

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

        verify(knowledgePathResolver)
                .resolve(
                        user,
                        summaryResponse.knowledgePath()
                );

        verify(knowledgeNoteRepository)
                .save(
                        captor.capture()
                );

        KnowledgeNote saved =
                captor.getValue();

        assertSummaryNote(
                saved,
                KnowledgeNoteType.INCREMENTAL
        );

        assertThat(saved.isIncremental())
                .isTrue();

        assertThat(saved.isConsolidated())
                .isFalse();

        assertThat(result)
                .isSameAs(saved);
    }

    @Test
    void AI_요약으로_Consolidated_노트를_저장한다() {
        // given
        mockResolvedKnowledge();

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

        verify(knowledgeNoteRepository)
                .save(
                        captor.capture()
                );

        KnowledgeNote saved =
                captor.getValue();

        assertSummaryNote(
                saved,
                KnowledgeNoteType.CONSOLIDATED
        );

        assertThat(saved.isConsolidated())
                .isTrue();

        assertThat(saved.isIncremental())
                .isFalse();

        assertThat(result)
                .isSameAs(saved);
    }

    @Test
    void AI_병합_결과로_Consolidated_노트의_내용과_경로를_갱신한다() {
        // given
        KnowledgeNote consolidatedNote =
                createConsolidatedNote(
                        currentKnowledge
                );

        ReflectionTestUtils.setField(
                consolidatedNote,
                "id",
                200L
        );

        when(
                knowledgePathResolver.resolve(
                        user,
                        mergeResponse.knowledgePath()
                )
        ).thenReturn(
                resolvedKnowledge
        );

        // when
        KnowledgeNote result =
                service.updateConsolidated(
                        user,
                        consolidatedNote,
                        mergeResponse
                );

        // then
        verify(knowledgePathResolver)
                .resolve(
                        user,
                        mergeResponse.knowledgePath()
                );

        verify(knowledgeNoteRepository, never())
                .save(
                        any(KnowledgeNote.class)
                );

        assertThat(result)
                .isSameAs(consolidatedNote);

        assertThat(consolidatedNote.getType())
                .isEqualTo(
                        KnowledgeNoteType.CONSOLIDATED
                );

        assertThat(consolidatedNote.getKnowledge())
                .isSameAs(resolvedKnowledge);

        assertMergeContent(
                consolidatedNote
        );
    }

    @Test
    void 병합된_경로가_기존과_같으면_내용만_갱신한다() {
        // given
        KnowledgeNote consolidatedNote =
                createConsolidatedNote(
                        resolvedKnowledge
                );

        when(
                knowledgePathResolver.resolve(
                        user,
                        mergeResponse.knowledgePath()
                )
        ).thenReturn(
                resolvedKnowledge
        );

        // when
        KnowledgeNote result =
                service.updateConsolidated(
                        user,
                        consolidatedNote,
                        mergeResponse
                );

        // then
        assertThat(result)
                .isSameAs(consolidatedNote);

        assertThat(consolidatedNote.getKnowledge())
                .isSameAs(resolvedKnowledge);

        assertMergeContent(
                consolidatedNote
        );
    }

    @Test
    void Incremental_노트는_통합_노트로_갱신할_수_없다() {
        // given
        KnowledgeNote incrementalNote =
                KnowledgeNote.createIncremental(
                        user,
                        conversation,
                        currentKnowledge,
                        "증분 제목",
                        "증분 설명",
                        "증분 요약",
                        List.of(
                                "Spring",
                                "Batch",
                                "Job"
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

        verify(knowledgePathResolver, never())
                .resolve(
                        any(),
                        any()
                );
    }

    private void mockResolvedKnowledge() {
        when(
                knowledgePathResolver.resolve(
                        user,
                        summaryResponse.knowledgePath()
                )
        ).thenReturn(
                resolvedKnowledge
        );
    }

    private KnowledgeNote createConsolidatedNote(
            Knowledge knowledge
    ) {
        return KnowledgeNote.createConsolidated(
                user,
                conversation,
                knowledge,
                "기존 제목",
                "기존 설명",
                "기존 요약",
                List.of(
                        "Spring Batch",
                        "Job",
                        "Step"
                )
        );
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
                .isSameAs(resolvedKnowledge);

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