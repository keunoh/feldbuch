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

    private static final Long USER_ID = 1L;

    @Mock
    private KnowledgeRepository knowledgeRepository;

    @Mock
    private KnowledgeRootCategoryResolver rootCategoryResolver;

    @Mock
    private KnowledgeFolderSelectionService folderSelectionService;

    private KnowledgePathResolver resolver;

    private User user;

    @BeforeEach
    void setUp() {
        resolver =
                new KnowledgePathResolver(
                        knowledgeRepository,
                        rootCategoryResolver,
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
                USER_ID
        );
    }

    @Test
    void 서버가_결정한_대분류와_하위_경로를_순서대로_생성한다() {
        // given
        List<String> path =
                List.of(
                        "Spring Framework",
                        "Spring WebFlux"
                );

        Knowledge root =
                createRootKnowledge(
                        10L,
                        "WEB_DEVELOPMENT"
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
                        "Spring WebFlux"
                );

        when(
                rootCategoryResolver.resolve(path)
        ).thenReturn(
                KnowledgeRootCategory.WEB_DEVELOPMENT
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "WEB_DEVELOPMENT"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                10L,
                                "Spring Framework"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                USER_ID,
                                10L
                        )
        ).thenReturn(
                List.of()
        );

        when(
                folderSelectionService.select(
                        "WEB_DEVELOPMENT",
                        "WEB_DEVELOPMENT",
                        "Spring Framework",
                        List.of()
                )
        ).thenReturn(
                createSelection()
        );

        when(
                knowledgeRepository.save(
                        any(Knowledge.class)
                )
        ).thenReturn(
                spring,
                webFlux
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                11L,
                                "Spring WebFlux"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                USER_ID,
                                11L
                        )
        ).thenReturn(
                List.of()
        );

        when(
                folderSelectionService.select(
                        "WEB_DEVELOPMENT",
                        "Spring Framework",
                        "Spring WebFlux",
                        List.of()
                )
        ).thenReturn(
                createSelection()
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        path
                );

        // then
        assertThat(result)
                .isSameAs(webFlux);

        InOrder inOrder =
                inOrder(
                        rootCategoryResolver,
                        knowledgeRepository,
                        folderSelectionService
                );

        inOrder.verify(rootCategoryResolver)
                .resolve(path);

        inOrder.verify(knowledgeRepository)
                .findByUserIdAndParentIsNullAndName(
                        USER_ID,
                        "WEB_DEVELOPMENT"
                );

        inOrder.verify(knowledgeRepository)
                .findByUserIdAndParentIdAndName(
                        USER_ID,
                        10L,
                        "Spring Framework"
                );

        inOrder.verify(knowledgeRepository)
                .findAllByUserIdAndParentIdOrderByNameAsc(
                        USER_ID,
                        10L
                );

        inOrder.verify(folderSelectionService)
                .select(
                        "WEB_DEVELOPMENT",
                        "WEB_DEVELOPMENT",
                        "Spring Framework",
                        List.of()
                );

        inOrder.verify(knowledgeRepository)
                .save(
                        any(Knowledge.class)
                );

        inOrder.verify(knowledgeRepository)
                .findByUserIdAndParentIdAndName(
                        USER_ID,
                        11L,
                        "Spring WebFlux"
                );

        inOrder.verify(knowledgeRepository)
                .findAllByUserIdAndParentIdOrderByNameAsc(
                        USER_ID,
                        11L
                );

        inOrder.verify(folderSelectionService)
                .select(
                        "WEB_DEVELOPMENT",
                        "Spring Framework",
                        "Spring WebFlux",
                        List.of()
                );

        inOrder.verify(knowledgeRepository)
                .save(
                        any(Knowledge.class)
                );
    }

    @Test
    void 동일한_이름의_하위_폴더가_있으면_AI를_호출하지_않고_재사용한다() {
        // given
        List<String> path =
                List.of("Spring Framework");

        Knowledge root =
                createRootKnowledge(
                        10L,
                        "WEB_DEVELOPMENT"
                );

        Knowledge spring =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring Framework"
                );

        when(
                rootCategoryResolver.resolve(path)
        ).thenReturn(
                KnowledgeRootCategory.WEB_DEVELOPMENT
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "WEB_DEVELOPMENT"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                10L,
                                "Spring Framework"
                        )
        ).thenReturn(
                Optional.of(spring)
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        path
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
                .save(
                        any(Knowledge.class)
                );
    }

    @Test
    void AI가_기존_폴더를_선택하면_해당_폴더를_재사용한다() {
        // given
        List<String> path =
                List.of("Spring Framework");

        Knowledge root =
                createRootKnowledge(
                        10L,
                        "WEB_DEVELOPMENT"
                );

        Knowledge spring =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring"
                );

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        KnowledgeFolderCandidate.from(
                                spring
                        )
                );

        when(
                rootCategoryResolver.resolve(path)
        ).thenReturn(
                KnowledgeRootCategory.WEB_DEVELOPMENT
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "WEB_DEVELOPMENT"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                10L,
                                "Spring Framework"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                USER_ID,
                                10L
                        )
        ).thenReturn(
                List.of(spring)
        );

        when(
                folderSelectionService.select(
                        "WEB_DEVELOPMENT",
                        "WEB_DEVELOPMENT",
                        "Spring Framework",
                        candidates
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
                                USER_ID
                        )
        ).thenReturn(
                Optional.of(spring)
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        path
                );

        // then
        assertThat(result)
                .isSameAs(spring);

        verify(knowledgeRepository, never())
                .save(
                        any(Knowledge.class)
                );
    }

    @Test
    void AI가_CREATE를_선택하면_새_폴더를_생성한다() {
        // given
        List<String> path =
                List.of("Spring Framework");

        Knowledge root =
                createRootKnowledge(
                        10L,
                        "WEB_DEVELOPMENT"
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

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        KnowledgeFolderCandidate.from(
                                react
                        )
                );

        when(
                rootCategoryResolver.resolve(path)
        ).thenReturn(
                KnowledgeRootCategory.WEB_DEVELOPMENT
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "WEB_DEVELOPMENT"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                10L,
                                "Spring Framework"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                USER_ID,
                                10L
                        )
        ).thenReturn(
                List.of(react)
        );

        when(
                folderSelectionService.select(
                        "WEB_DEVELOPMENT",
                        "WEB_DEVELOPMENT",
                        "Spring Framework",
                        candidates
                )
        ).thenReturn(
                createSelection()
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
                        path
                );

        // then
        assertThat(result)
                .isSameAs(newFolder);

        ArgumentCaptor<Knowledge> captor =
                ArgumentCaptor.forClass(
                        Knowledge.class
                );

        verify(knowledgeRepository)
                .save(
                        captor.capture()
                );

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
    void AI가_선택한_기존_폴더를_찾을_수_없으면_실패한다() {
        // given
        List<String> path =
                List.of("Spring Framework");

        Knowledge root =
                createRootKnowledge(
                        10L,
                        "WEB_DEVELOPMENT"
                );

        Knowledge spring =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring"
                );

        List<KnowledgeFolderCandidate> candidates =
                List.of(
                        KnowledgeFolderCandidate.from(
                                spring
                        )
                );

        when(
                rootCategoryResolver.resolve(path)
        ).thenReturn(
                KnowledgeRootCategory.WEB_DEVELOPMENT
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "WEB_DEVELOPMENT"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                10L,
                                "Spring Framework"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                USER_ID,
                                10L
                        )
        ).thenReturn(
                List.of(spring)
        );

        when(
                folderSelectionService.select(
                        "WEB_DEVELOPMENT",
                        "WEB_DEVELOPMENT",
                        "Spring Framework",
                        candidates
                )
        ).thenReturn(
                new AiKnowledgeFolderSelectionResponse(
                        AiKnowledgeFolderSelectionType.EXISTING,
                        999L
                )
        );

        when(
                knowledgeRepository
                        .findByIdAndUserId(
                                999L,
                                USER_ID
                        )
        ).thenReturn(
                Optional.empty()
        );

        // when & then
        assertThatThrownBy(() ->
                resolver.resolve(
                        user,
                        path
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "knowledgeId=999"
                );
    }

    @Test
    void 하위_경로가_2단계를_초과하면_실패한다() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        user,
                        List.of(
                                "Spring Framework",
                                "Spring WebFlux",
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

        verify(rootCategoryResolver, never())
                .resolve(any());
    }

    @Test
    void 하위_경로가_null이면_실패한다() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        user,
                        null
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Knowledge 하위 경로는 필수입니다."
                );
    }

    @Test
    void 하위_경로가_비어있으면_실패한다() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        user,
                        List.of()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Knowledge 하위 경로는 필수입니다."
                );
    }

    @Test
    void 유효한_하위_경로가_없으면_실패한다() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        user,
                        List.of(
                                " ",
                                "\t"
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Knowledge 하위 경로에 유효한 이름이 없습니다."
                );
    }

    @Test
    void 사용자가_null이면_실패한다() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        null,
                        List.of(
                                "Spring Framework"
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "사용자는 필수입니다."
                );
    }

    @Test
    void 저장되지_않은_사용자이면_실패한다() {
        // given
        User unsavedUser =
                User.builder()
                        .email("unsaved@test.com")
                        .password("password")
                        .nickname("unsaved")
                        .build();

        // when & then
        assertThatThrownBy(() ->
                resolver.resolve(
                        unsavedUser,
                        List.of(
                                "Spring Framework"
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "저장되지 않은 사용자는 Knowledge 경로를 생성할 수 없습니다."
                );
    }

    private AiKnowledgeFolderSelectionResponse createSelection() {
        return new AiKnowledgeFolderSelectionResponse(
                AiKnowledgeFolderSelectionType.CREATE,
                null
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