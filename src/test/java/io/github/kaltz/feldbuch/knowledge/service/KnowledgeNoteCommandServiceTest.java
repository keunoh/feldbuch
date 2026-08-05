package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeMergeResponse;
import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeNoteCommandServiceTest {

    @Mock
    private KnowledgePathResolver knowledgePathResolver;

    @Mock
    private KnowledgeNoteRepository knowledgeNoteRepository;

    @InjectMocks
    private KnowledgeNoteCommandService knowledgeNoteCommandService;

    private User user;

    private Conversation conversation;

    private Knowledge currentKnowledge;

    private Knowledge resolvedKnowledge;

    private AiKnowledgeSummaryResponse summaryResponse;

    private AiKnowledgeMergeResponse mergeResponse;

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
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of(
                                "Spring Framework",
                                "Spring Batch"
                        ),
                        "Spring Batch 기본 구조",
                        "Job과 Step을 중심으로 실행 구조를 정리한 노트",
                        "Spring Batch는 Job과 Step을 중심으로 대용량 일괄 처리를 구성합니다.",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        mergeResponse =
                new AiKnowledgeMergeResponse(
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of(
                                "Spring Framework",
                                "Spring Batch"
                        ),
                        "Spring Batch 처리 방식",
                        "Tasklet과 Chunk 방식까지 반영한 Spring Batch 학습 노트",
                        "Spring Batch는 Job과 Step으로 구성되며 Step에서는 Tasklet 또는 Chunk 기반 처리를 사용할 수 있습니다.",
                        List.of(
                                "Spring Batch",
                                "Tasklet",
                                "Chunk"
                        )
                );
    }

    @Test
    void AI_요약으로_새_KnowledgeNote를_저장한다() {
        // given
        when(
                knowledgePathResolver.resolve(
                        user,
                        summaryResponse.rootCategory(),
                        summaryResponse.knowledgePath()
                )
        ).thenReturn(
                resolvedKnowledge
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
                knowledgeNoteCommandService.saveAiSummary(
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
                        summaryResponse.rootCategory(),
                        summaryResponse.knowledgePath()
                );

        verify(knowledgeNoteRepository)
                .save(
                        captor.capture()
                );

        KnowledgeNote saved =
                captor.getValue();

        assertThat(saved.getUser())
                .isSameAs(user);

        assertThat(saved.getConversation())
                .isSameAs(conversation);

        assertThat(saved.getKnowledge())
                .isSameAs(resolvedKnowledge);

        assertThat(saved.getTitle())
                .isEqualTo(
                        summaryResponse.title()
                );

        assertThat(saved.getDescription())
                .isEqualTo(
                        summaryResponse.description()
                );

        assertThat(saved.getSummary())
                .isEqualTo(
                        summaryResponse.summary()
                );

        assertThat(saved.getKeywords())
                .containsExactlyElementsOf(
                        summaryResponse.keywords()
                );

        assertThat(result)
                .isSameAs(saved);
    }

    @Test
    void AI_병합_결과로_기존_KnowledgeNote를_갱신한다() {
        // given
        KnowledgeNote existingNote =
                KnowledgeNote.create(
                        user,
                        conversation,
                        currentKnowledge,
                        "기존 제목",
                        "기존 설명",
                        "기존 요약",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        ReflectionTestUtils.setField(
                existingNote,
                "id",
                200L
        );

        when(
                knowledgePathResolver.resolve(
                        user,
                        mergeResponse.rootCategory(),
                        mergeResponse.knowledgePath()
                )
        ).thenReturn(
                resolvedKnowledge
        );

        // when
        KnowledgeNote result =
                knowledgeNoteCommandService.updateAiSummary(
                        user,
                        existingNote,
                        mergeResponse
                );

        // then
        verify(knowledgePathResolver)
                .resolve(
                        user,
                        mergeResponse.rootCategory(),
                        mergeResponse.knowledgePath()
                );

        verify(knowledgeNoteRepository, never())
                .save(
                        any(KnowledgeNote.class)
                );

        assertThat(result)
                .isSameAs(existingNote);

        assertThat(existingNote.getKnowledge())
                .isSameAs(resolvedKnowledge);

        assertThat(existingNote.getTitle())
                .isEqualTo(
                        mergeResponse.title()
                );

        assertThat(existingNote.getDescription())
                .isEqualTo(
                        mergeResponse.description()
                );

        assertThat(existingNote.getSummary())
                .isEqualTo(
                        mergeResponse.summary()
                );

        assertThat(existingNote.getKeywords())
                .containsExactlyElementsOf(
                        mergeResponse.keywords()
                );
    }

    @Test
    void 병합_결과의_Knowledge가_기존과_같으면_이동하지_않고_내용만_갱신한다() {
        // given
        KnowledgeNote existingNote =
                KnowledgeNote.create(
                        user,
                        conversation,
                        resolvedKnowledge,
                        "기존 제목",
                        "기존 설명",
                        "기존 요약",
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        when(
                knowledgePathResolver.resolve(
                        user,
                        mergeResponse.rootCategory(),
                        mergeResponse.knowledgePath()
                )
        ).thenReturn(
                resolvedKnowledge
        );

        // when
        KnowledgeNote result =
                knowledgeNoteCommandService.updateAiSummary(
                        user,
                        existingNote,
                        mergeResponse
                );

        // then
        assertThat(result)
                .isSameAs(existingNote);

        assertThat(existingNote.getKnowledge())
                .isSameAs(resolvedKnowledge);

        assertThat(existingNote.getTitle())
                .isEqualTo(
                        mergeResponse.title()
                );

        assertThat(existingNote.getDescription())
                .isEqualTo(
                        mergeResponse.description()
                );

        assertThat(existingNote.getSummary())
                .isEqualTo(
                        mergeResponse.summary()
                );

        assertThat(existingNote.getKeywords())
                .containsExactlyElementsOf(
                        mergeResponse.keywords()
                );
    }
}