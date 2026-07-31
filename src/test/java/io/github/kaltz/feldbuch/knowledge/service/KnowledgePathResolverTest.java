package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgePathResolverTest {

    @Mock
    private KnowledgeRepository knowledgeRepository;

    @Mock
    private User user;

    private KnowledgePathResolver knowledgePathResolver;

    private final AtomicLong idSequence = new AtomicLong(1L);

    @BeforeEach
    void setUp() {
        knowledgePathResolver =
                new KnowledgePathResolver(knowledgeRepository);

        when(user.getId()).thenReturn(1L);

        /**
         * 실제 JPA에서는 save() 후 ID가 자동 생성된다.
         *
         * 단위 테스트에서는 JPA가 동작하지 않으므로
         * ReflectionTestUtils를 이용해 가짜 ID를 부여한다.
         */
        lenient()
                .when(knowledgeRepository.save(any(Knowledge.class)))
                .thenAnswer(invocation -> {
                    Knowledge knowledge = invocation.getArgument(0);

                    if (knowledge.getId() == null) {
                        ReflectionTestUtils.setField(
                                knowledge,
                                "id",
                                idSequence.getAndIncrement()
                        );
                    }

                    return knowledge;
                });
    }

    @Test
    @DisplayName("Knowledge 경로가 존재하지 않으면 순서대로 생성한다")
    void resolve_createsMissingKnowledgePath() {
        // given
        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                1L,
                                "개발"
                        )
        ).thenReturn(Optional.empty());

        when(
                knowledgeRepository.findByUserIdAndParentIdAndName(
                        1L,
                        1L,
                        "Spring"
                )
        ).thenReturn(Optional.empty());

        when(
                knowledgeRepository.findByUserIdAndParentIdAndName(
                        1L,
                        2L,
                        "JPA"
                )
        ).thenReturn(Optional.empty());

        // when
        Knowledge result = knowledgePathResolver.resolve(
                user,
                List.of(
                        "개발",
                        "Spring",
                        "JPA"
                )
        );

        // then
        assertThat(result.getName()).isEqualTo("JPA");
        assertThat(result.getId()).isEqualTo(3L);

        assertThat(result.getParent())
                .isNotNull()
                .extracting(Knowledge::getName)
                .isEqualTo("Spring");

        assertThat(result.getParent().getParent())
                .isNotNull()
                .extracting(Knowledge::getName)
                .isEqualTo("개발");

        verify(knowledgeRepository, times(3))
                .save(any(Knowledge.class));
    }

    @Test
    @DisplayName("이미 존재하는 Knowledge 경로는 새로 생성하지 않고 재사용한다")
    void resolve_reusesExistingKnowledgePath() {
        // given
        Knowledge development = Knowledge.createRoot(
                user,
                "개발"
        );

        ReflectionTestUtils.setField(
                development,
                "id",
                10L
        );

        Knowledge spring = Knowledge.createChild(
                user,
                development,
                "Spring"
        );

        ReflectionTestUtils.setField(
                spring,
                "id",
                20L
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                1L,
                                "개발"
                        )
        ).thenReturn(Optional.of(development));

        when(
                knowledgeRepository.findByUserIdAndParentIdAndName(
                        1L,
                        10L,
                        "Spring"
                )
        ).thenReturn(Optional.of(spring));

        // when
        Knowledge result = knowledgePathResolver.resolve(
                user,
                List.of(
                        "개발",
                        "Spring"
                )
        );

        // then
        assertThat(result).isSameAs(spring);
        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getName()).isEqualTo("Spring");

        verify(knowledgeRepository, never())
                .save(any(Knowledge.class));
    }

    @Test
    @DisplayName("Knowledge 경로의 앞뒤 공백과 빈 항목을 정리한다")
    void resolve_normalizesKnowledgePath() {
        // given
        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                1L,
                                "개발"
                        )
        ).thenReturn(Optional.empty());

        when(
                knowledgeRepository.findByUserIdAndParentIdAndName(
                        1L,
                        1L,
                        "Spring"
                )
        ).thenReturn(Optional.empty());

        // when
        Knowledge result = knowledgePathResolver.resolve(
                user,
                List.of(
                        "  개발  ",
                        "",
                        "  Spring  "
                )
        );

        // then
        assertThat(result.getName()).isEqualTo("Spring");
        assertThat(result.getParent().getName()).isEqualTo("개발");

        verify(knowledgeRepository).findByUserIdAndParentIdAndName(
                1L,
                1L,
                "Spring"
        );
    }

    @Test
    @DisplayName("Knowledge 경로가 비어 있으면 예외가 발생한다")
    void resolve_rejectsEmptyPath() {
        assertThatThrownBy(
                () -> knowledgePathResolver.resolve(
                        user,
                        List.of()
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Knowledge 경로는 필수입니다.");

        verifyNoInteractions(knowledgeRepository);
    }

    @Test
    @DisplayName("저장되지 않은 사용자는 Knowledge 경로를 만들 수 없다")
    void resolve_rejectsUnsavedUser() {
        // given
        when(user.getId()).thenReturn(null);

        // when, then
        assertThatThrownBy(
                () -> knowledgePathResolver.resolve(
                        user,
                        List.of("개발")
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "저장되지 않은 사용자는 Knowledge 경로를 생성할 수 없습니다."
                );

        verifyNoInteractions(knowledgeRepository);
    }
}