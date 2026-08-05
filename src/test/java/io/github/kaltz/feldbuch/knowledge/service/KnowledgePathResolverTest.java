package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;
import io.github.kaltz.feldbuch.knowledge.folder.AiKnowledgeFolderSelectionResponse;
import io.github.kaltz.feldbuch.knowledge.folder.AiKnowledgeFolderSelectionType;
import io.github.kaltz.feldbuch.knowledge.folder.KnowledgeFolderCandidate;
import io.github.kaltz.feldbuch.knowledge.folder.KnowledgeFolderSelectionService;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgePathResolverTest {

    @Mock
    private KnowledgeRepository knowledgeRepository;

    @Mock
    private KnowledgeFolderSelectionService folderSelectionService;

    private KnowledgePathResolver resolver;

    private User user;

    @BeforeEach
    void setUp() {
        resolver =
                new KnowledgePathResolver(
                        knowledgeRepository,
                        folderSelectionService
                );

        user =
                User.builder()
                        .email("test@test.com")
                        .password("password")
                        .nickname("tester")
                        .build();

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );
    }

    @Test
    void 고정_대분류와_하위_경로를_순서대로_생성한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "웹 개발"
                );

        Knowledge spring =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring Framework"
                );

        Knowledge webFlux =
                createChildKnowledge(
                        12L,
                        spring,
                        "WebFlux"
                );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                1L,
                                "웹 개발"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                1L,
                                10L,
                                "Spring Framework"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                1L,
                                10L
                        )
        ).thenReturn(
                List.of()
        );

        when(
                folderSelectionService.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        List.of()
                )
        ).thenReturn(
                new AiKnowledgeFolderSelectionResponse(
                        AiKnowledgeFolderSelectionType.CREATE,
                        null
                )
        );

        when(knowledgeRepository.save(any(Knowledge.class)))
                .thenReturn(spring, webFlux);

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                1L,
                                11L,
                                "WebFlux"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                1L,
                                11L
                        )
        ).thenReturn(
                List.of()
        );

        when(
                folderSelectionService.select(
                        "웹 개발",
                        "Spring Framework",
                        "WebFlux",
                        List.of()
                )
        ).thenReturn(
                new AiKnowledgeFolderSelectionResponse(
                        AiKnowledgeFolderSelectionType.CREATE,
                        null
                )
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of(
                                "Spring Framework",
                                "WebFlux"
                        )
                );

        // then
        assertThat(result)
                .isSameAs(webFlux);

        InOrder inOrder =
                inOrder(
                        knowledgeRepository,
                        folderSelectionService
                );

        inOrder.verify(knowledgeRepository)
                .findByUserIdAndParentIsNullAndName(
                        1L,
                        "웹 개발"
                );

        inOrder.verify(knowledgeRepository)
                .findByUserIdAndParentIdAndName(
                        1L,
                        10L,
                        "Spring Framework"
                );

        inOrder.verify(knowledgeRepository)
                .findAllByUserIdAndParentIdOrderByNameAsc(
                        1L,
                        10L
                );

        inOrder.verify(folderSelectionService)
                .select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        List.of()
                );

        inOrder.verify(knowledgeRepository)
                .save(any(Knowledge.class));

        inOrder.verify(knowledgeRepository)
                .findByUserIdAndParentIdAndName(
                        1L,
                        11L,
                        "WebFlux"
                );

        inOrder.verify(knowledgeRepository)
                .findAllByUserIdAndParentIdOrderByNameAsc(
                        1L,
                        11L
                );

        inOrder.verify(folderSelectionService)
                .select(
                        "웹 개발",
                        "Spring Framework",
                        "WebFlux",
                        List.of()
                );

        inOrder.verify(knowledgeRepository)
                .save(any(Knowledge.class));
    }

    @Test
    void 동일한_이름의_폴더가_있으면_AI를_호출하지_않고_재사용한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "웹 개발"
                );

        Knowledge spring =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring"
                );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                1L,
                                "웹 개발"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                1L,
                                10L,
                                "Spring"
                        )
        ).thenReturn(
                Optional.of(spring)
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of("Spring")
                );

        // then
        assertThat(result)
                .isSameAs(spring);

        verify(folderSelectionService, never())
                .select(
                        any(),
                        any(),
                        any(),
                        any()
                );

        verify(knowledgeRepository, never())
                .save(any(Knowledge.class));
    }

    @Test
    void AI가_기존_폴더를_선택하면_해당_폴더를_재사용한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "웹 개발"
                );

        Knowledge spring =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring"
                );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                1L,
                                "웹 개발"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                1L,
                                10L,
                                "Spring Framework"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                1L,
                                10L
                        )
        ).thenReturn(
                List.of(spring)
        );

        when(
                folderSelectionService.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        List.of(
                                KnowledgeFolderCandidate.from(
                                        spring
                                )
                        )
                )
        ).thenReturn(
                new AiKnowledgeFolderSelectionResponse(
                        AiKnowledgeFolderSelectionType.EXISTING,
                        11L
                )
        );

        when(
                knowledgeRepository
                        .findByIdAndUserId(
                                11L,
                                1L
                        )
        ).thenReturn(
                Optional.of(spring)
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of(
                                "Spring Framework"
                        )
                );

        // then
        assertThat(result)
                .isSameAs(spring);

        verify(knowledgeRepository, never())
                .save(any(Knowledge.class));
    }

    @Test
    void AI가_CREATE를_선택하면_새_폴더를_생성한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "웹 개발"
                );

        Knowledge react =
                createChildKnowledge(
                        11L,
                        root,
                        "React"
                );

        Knowledge newFolder =
                createChildKnowledge(
                        12L,
                        root,
                        "Spring Framework"
                );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                1L,
                                "웹 개발"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                1L,
                                10L,
                                "Spring Framework"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                1L,
                                10L
                        )
        ).thenReturn(
                List.of(react)
        );

        when(
                folderSelectionService.select(
                        "웹 개발",
                        "웹 개발",
                        "Spring Framework",
                        List.of(
                                KnowledgeFolderCandidate.from(
                                        react
                                )
                        )
                )
        ).thenReturn(
                new AiKnowledgeFolderSelectionResponse(
                        AiKnowledgeFolderSelectionType.CREATE,
                        null
                )
        );

        when(
                knowledgeRepository.save(
                        any(Knowledge.class)
                )
        ).thenReturn(
                newFolder
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of(
                                "Spring Framework"
                        )
                );

        // then
        assertThat(result)
                .isSameAs(newFolder);

        ArgumentCaptor<Knowledge> captor =
                ArgumentCaptor.forClass(
                        Knowledge.class
                );

        verify(knowledgeRepository)
                .save(captor.capture());

        Knowledge saved =
                captor.getValue();

        assertThat(saved.getName())
                .isEqualTo(
                        "Spring Framework"
                );

        assertThat(saved.getParent())
                .isSameAs(root);
    }

    @Test
    void 하위_경로가_2단계를_초과하면_실패한다() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        user,
                        KnowledgeRootCategory.WEB_DEVELOPMENT,
                        List.of(
                                "Spring",
                                "WebFlux",
                                "Reactive Streams"
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Knowledge 하위 경로는 최대 2단계까지 허용됩니다."
                );
    }

    @Test
    void 대분류가_null이면_실패한다() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        user,
                        null,
                        List.of("Spring")
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Knowledge 대분류는 필수입니다."
                );
    }

    private Knowledge createRootKnowledge(
            Long id,
            String name
    ) {
        Knowledge knowledge =
                Knowledge.createRoot(
                        user,
                        name
                );

        ReflectionTestUtils.setField(
                knowledge,
                "id",
                id
        );

        return knowledge;
    }

    private Knowledge createChildKnowledge(
            Long id,
            Knowledge parent,
            String name
    ) {
        Knowledge knowledge =
                Knowledge.createChild(
                        user,
                        parent,
                        name
                );

        ReflectionTestUtils.setField(
                knowledge,
                "id",
                id
        );

        return knowledge;
    }
}