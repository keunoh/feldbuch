package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeNoteDetailResponse;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeNoteSummaryResponse;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeTreeResponse;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNoteType;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeQueryServiceTest {

    @Mock
    private KnowledgeRepository knowledgeRepository;

    @Mock
    private Knowledge development;

    @Mock
    private Knowledge spring;

    @Mock
    private Knowledge webFlux;

    @Mock
    private Knowledge ai;

    @Mock
    private KnowledgeNoteRepository knowledgeNoteRepository;

    @Mock
    private KnowledgeNote firstNote;

    @Mock
    private KnowledgeNote secondNote;

    @InjectMocks
    private KnowledgeQueryService knowledgeQueryService;

    @Test
    void Knowledge_목록을_트리_구조로_변환한다() {
        // given
        Long userId = 1L;

        when(development.getId())
                .thenReturn(1L);
        when(development.getName())
                .thenReturn("개발");
        when(development.getParent())
                .thenReturn(null);

        when(spring.getId())
                .thenReturn(2L);
        when(spring.getName())
                .thenReturn("Spring");
        when(spring.getParent())
                .thenReturn(development);

        when(webFlux.getId())
                .thenReturn(3L);
        when(webFlux.getName())
                .thenReturn("Spring WebFlux");
        when(webFlux.getParent())
                .thenReturn(spring);

        when(ai.getId())
                .thenReturn(4L);
        when(ai.getName())
                .thenReturn("AI");
        when(ai.getParent())
                .thenReturn(null);
        when(
                knowledgeRepository
                        .findAllByUserIdOrderByCreatedAtAsc(
                                userId
                        )
        ).thenReturn(
                List.of(
                        development,
                        spring,
                        webFlux,
                        ai
                )
        );

        // when
        List<KnowledgeTreeResponse> result =
                knowledgeQueryService.findTree(userId);

        // then
        assertThat(result)
                .hasSize(2);

        KnowledgeTreeResponse developmentNode =
                result.getFirst();

        assertThat(developmentNode.id())
                .isEqualTo(1L);
        assertThat(developmentNode.name())
                .isEqualTo("개발");
        assertThat(developmentNode.children())
                .hasSize(1);

        KnowledgeTreeResponse springNode =
                developmentNode.children().getFirst();

        assertThat(springNode.id())
                .isEqualTo(2L);
        assertThat(springNode.name())
                .isEqualTo("Spring");
        assertThat(springNode.children())
                .hasSize(1);

        KnowledgeTreeResponse webFluxNode =
                springNode.children().getFirst();

        assertThat(webFluxNode.id())
                .isEqualTo(3L);
        assertThat(webFluxNode.name())
                .isEqualTo("Spring WebFlux");
        assertThat(webFluxNode.children())
                .isEmpty();

        KnowledgeTreeResponse aiNode =
                result.get(1);

        assertThat(aiNode.name())
                .isEqualTo("AI");
        assertThat(aiNode.children())
                .isEmpty();

        verify(knowledgeRepository)
                .findAllByUserIdOrderByCreatedAtAsc(
                        userId
                );
    }

    @Test
    void Knowledge가_없으면_빈_트리를_반환한다() {
        // given
        Long userId = 1L;

        when(
                knowledgeRepository
                        .findAllByUserIdOrderByCreatedAtAsc(
                                userId
                        )
        ).thenReturn(List.of());

        // when
        List<KnowledgeTreeResponse> result =
                knowledgeQueryService.findTree(userId);

        // then
        assertThat(result).isEmpty();

        verify(knowledgeRepository)
                .findAllByUserIdOrderByCreatedAtAsc(
                        userId
                );
    }

    @Test
    void Knowledge에_속한_노트_목록을_조회한다() {
        // given
        Long userId = 1L;
        Long knowledgeId = 10L;

        LocalDateTime firstCreatedAt =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        10,
                        0
                );

        LocalDateTime secondCreatedAt =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        11,
                        0
                );

        when(firstNote.getId())
                .thenReturn(100L);

        when(firstNote.getTitle())
                .thenReturn(
                        "Spring Batch 기본 구조"
                );

        when(firstNote.getSummary())
                .thenReturn(
                        "Job과 Step을 중심으로 정리한 내용"
                );

        when(firstNote.getCreatedAt())
                .thenReturn(firstCreatedAt);

        when(secondNote.getId())
                .thenReturn(101L);

        when(secondNote.getTitle())
                .thenReturn(
                        "Tasklet과 Chunk"
                );

        when(secondNote.getSummary())
                .thenReturn(
                        "Tasklet과 Chunk 처리 방식을 비교한 내용"
                );

        when(secondNote.getCreatedAt())
                .thenReturn(secondCreatedAt);

        when(
                knowledgeNoteRepository
                        .findAllByUserIdAndKnowledgeIdAndTypeOrderByCreatedAtDesc(
                                userId,
                                knowledgeId,
                                KnowledgeNoteType.INCREMENTAL
                        )
        ).thenReturn(
                List.of(
                        secondNote,
                        firstNote
                )
        );

        // when
        List<KnowledgeNoteSummaryResponse> result =
                knowledgeQueryService.findNotes(
                        userId,
                        knowledgeId
                );

        // then
        assertThat(result)
                .hasSize(2);

        assertThat(result.getFirst().id())
                .isEqualTo(101L);

        assertThat(result.getFirst().title())
                .isEqualTo(
                        "Tasklet과 Chunk"
                );

        assertThat(result.getFirst().summary())
                .isEqualTo(
                        "Tasklet과 Chunk 처리 방식을 비교한 내용"
                );

        assertThat(result.getFirst().createdAt())
                .isEqualTo(secondCreatedAt);

        assertThat(result.get(1).id())
                .isEqualTo(100L);

        assertThat(result.get(1).title())
                .isEqualTo(
                        "Spring Batch 기본 구조"
                );

        assertThat(result.get(1).summary())
                .isEqualTo(
                        "Job과 Step을 중심으로 정리한 내용"
                );

        assertThat(result.get(1).createdAt())
                .isEqualTo(firstCreatedAt);

        verify(knowledgeNoteRepository)
                .findAllByUserIdAndKnowledgeIdAndTypeOrderByCreatedAtDesc(
                        userId,
                        knowledgeId,
                        KnowledgeNoteType.INCREMENTAL
                );
    }

    @Test
    void Knowledge에_노트가_없으면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;
        Long knowledgeId = 10L;

        when(
                knowledgeNoteRepository
                        .findAllByUserIdAndKnowledgeIdAndTypeOrderByCreatedAtDesc(
                                userId,
                                knowledgeId,
                                KnowledgeNoteType.INCREMENTAL
                        )
        ).thenReturn(List.of());

        // when
        List<KnowledgeNoteSummaryResponse> result =
                knowledgeQueryService.findNotes(userId, knowledgeId);

        // then
        assertThat(result).isEmpty();

        verify(knowledgeNoteRepository)
                .findAllByUserIdAndKnowledgeIdAndTypeOrderByCreatedAtDesc(
                        userId,
                        knowledgeId,
                        KnowledgeNoteType.INCREMENTAL
                );
    }

    @Test
    void KnowledgeNote를_상세조회한다() {
        // given
        Long userId = 1L;
        Long noteId = 100L;

        when(
                knowledgeNoteRepository
                        .findByIdAndUserId(
                                noteId,
                                userId
                        )
        ).thenReturn(
                Optional.of(firstNote)
        );

        when(firstNote.getId())
                .thenReturn(noteId);

        when(firstNote.getTitle())
                .thenReturn("Spring Batch 기본 구조");

        when(firstNote.getDescription())
                .thenReturn(
                        "Job과 Step을 중심으로 실행 구조를 정리한 노트"
                );

        when(firstNote.getSummary())
                .thenReturn(
                        "Spring Batch는 대용량 일괄 처리를 지원합니다."
                );

        when(firstNote.getKeywords())
                .thenReturn(
                        List.of(
                                "Spring Batch",
                                "Job",
                                "Step"
                        )
                );

        // when
        KnowledgeNoteDetailResponse result =
                knowledgeQueryService.findNote(
                        userId,
                        noteId
                );

        // then
        assertThat(result.id())
                .isEqualTo(noteId);

        assertThat(result.title())
                .isEqualTo("Spring Batch 기본 구조");

        assertThat(result.description())
                .isEqualTo(
                        "Job과 Step을 중심으로 실행 구조를 정리한 노트"
                );

        assertThat(result.summary())
                .isEqualTo(
                        "Spring Batch는 대용량 일괄 처리를 지원합니다."
                );

        assertThat(result.keywords())
                .containsExactly(
                        "Spring Batch",
                        "Job",
                        "Step"
                );

        verify(knowledgeNoteRepository)
                .findByIdAndUserId(
                        noteId,
                        userId
                );
    }

    @Test
    void 존재하지_않는_KnowledgeNote를_조회하면_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long noteId = 999L;

        when(
                knowledgeNoteRepository
                        .findByIdAndUserId(
                                noteId,
                                userId
                        )
        ).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                knowledgeQueryService.findNote(
                        userId,
                        noteId
                )
        )
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException =
                            (CustomException) exception;

                    assertThat(customException.getErrorCode())
                            .isEqualTo(
                                    ErrorCode.KNOWLEDGE_NOTE_NOT_FOUND
                            );
                });

        verify(knowledgeNoteRepository)
                .findByIdAndUserId(
                        noteId,
                        userId
                );
    }
}