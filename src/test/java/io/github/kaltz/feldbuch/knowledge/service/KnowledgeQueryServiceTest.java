package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeTreeResponse;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
}