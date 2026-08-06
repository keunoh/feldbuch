package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeCategoryResolverTest {

    private static final Long USER_ID = 1L;

    @Mock
    private KnowledgeRepository knowledgeRepository;

    private KnowledgeCategoryResolver resolver;

    private User user;

    @BeforeEach
    void setUp() {
        resolver =
                new KnowledgeCategoryResolver(
                        knowledgeRepository
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
    }

    @Test
    void Spring_Batch_카테고리를_WEB_DEVELOPMENT_아래에_생성한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "WEB_DEVELOPMENT"
                );

        Knowledge springBatch =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring Batch"
                );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "WEB_DEVELOPMENT"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository.save(
                        any(Knowledge.class)
                )
        ).thenReturn(
                root,
                springBatch
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                10L,
                                "Spring Batch"
                        )
        ).thenReturn(
                Optional.empty()
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeCategory.SPRING_BATCH
                );

        // then
        assertThat(result)
                .isSameAs(springBatch);

        InOrder inOrder =
                inOrder(
                        knowledgeRepository
                );

        inOrder.verify(knowledgeRepository)
                .findByUserIdAndParentIsNullAndName(
                        USER_ID,
                        "WEB_DEVELOPMENT"
                );

        inOrder.verify(knowledgeRepository)
                .save(
                        any(Knowledge.class)
                );

        inOrder.verify(knowledgeRepository)
                .findByUserIdAndParentIdAndName(
                        USER_ID,
                        10L,
                        "Spring Batch"
                );

        inOrder.verify(knowledgeRepository)
                .save(
                        any(Knowledge.class)
                );
    }

    @Test
    void JPA_카테고리를_DATABASE_아래에_생성한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        20L,
                        "DATABASE"
                );

        Knowledge jpa =
                createChildKnowledge(
                        21L,
                        root,
                        "JPA"
                );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "DATABASE"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository.save(
                        any(Knowledge.class)
                )
        ).thenReturn(
                root,
                jpa
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                20L,
                                "JPA"
                        )
        ).thenReturn(
                Optional.empty()
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeCategory.JPA
                );

        // then
        assertThat(result)
                .isSameAs(jpa);

        assertThat(result.getName())
                .isEqualTo(
                        "JPA"
                );

        assertThat(result.getParent())
                .isSameAs(root);

        assertThat(result.getParent().getName())
                .isEqualTo(
                        "DATABASE"
                );
    }

    @Test
    void 루트와_카테고리_폴더가_이미_있으면_재사용한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "WEB_DEVELOPMENT"
                );

        Knowledge springBatch =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring Batch"
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
                                "Spring Batch"
                        )
        ).thenReturn(
                Optional.of(springBatch)
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeCategory.SPRING_BATCH
                );

        // then
        assertThat(result)
                .isSameAs(springBatch);

        verify(knowledgeRepository, never())
                .save(
                        any(Knowledge.class)
                );
    }

    @Test
    void 루트만_있으면_카테고리_폴더만_생성한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "WEB_DEVELOPMENT"
                );

        Knowledge springSecurity =
                createChildKnowledge(
                        11L,
                        root,
                        "Spring Security"
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
                                "Spring Security"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository.save(
                        any(Knowledge.class)
                )
        ).thenReturn(
                springSecurity
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeCategory.SPRING_SECURITY
                );

        // then
        assertThat(result)
                .isSameAs(springSecurity);

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
                        "Spring Security"
                );

        assertThat(saved.getParent())
                .isSameAs(root);
    }

    @Test
    void 루트가_없으면_enum_이름으로_생성한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "DEVOPS"
                );

        Knowledge docker =
                createChildKnowledge(
                        11L,
                        root,
                        "Docker"
                );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "DEVOPS"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository.save(
                        any(Knowledge.class)
                )
        ).thenReturn(
                root,
                docker
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                10L,
                                "Docker"
                        )
        ).thenReturn(
                Optional.empty()
        );

        ArgumentCaptor<Knowledge> captor =
                ArgumentCaptor.forClass(
                        Knowledge.class
                );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeCategory.DOCKER
                );

        // then
        assertThat(result)
                .isSameAs(docker);

        verify(knowledgeRepository,
                Mockito.times(2)
        )
                .save(
                        captor.capture()
                );

        List<Knowledge> saved =
                captor.getAllValues();

        Knowledge rootCategory = saved.get(0);
        Knowledge category = saved.get(1);

        assertThat(rootCategory.getName())
                .isEqualTo("DEVOPS");

        assertThat(rootCategory.isRoot())
                .isTrue();

        assertThat(category.getName())
                .isEqualTo("Docker");

        assertThat(category.getParent())
                .isSameAs(root);
    }

    @Test
    void 카테고리의_displayName으로_하위_폴더를_생성한다() {
        // given
        Knowledge root =
                createRootKnowledge(
                        10L,
                        "DEVOPS"
                );

        Knowledge githubActions =
                createChildKnowledge(
                        11L,
                        root,
                        "GitHub Actions"
                );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIsNullAndName(
                                USER_ID,
                                "DEVOPS"
                        )
        ).thenReturn(
                Optional.of(root)
        );

        when(
                knowledgeRepository
                        .findByUserIdAndParentIdAndName(
                                USER_ID,
                                10L,
                                "GitHub Actions"
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                knowledgeRepository.save(
                        any(Knowledge.class)
                )
        ).thenReturn(
                githubActions
        );

        // when
        Knowledge result =
                resolver.resolve(
                        user,
                        KnowledgeCategory.GITHUB_ACTIONS
                );

        // then
        assertThat(result.getName())
                .isEqualTo(
                        KnowledgeCategory.GITHUB_ACTIONS
                                .getDisplayName()
                );

        assertThat(result.getParent())
                .isSameAs(root);
    }

    @Test
    void 사용자가_null이면_실패한다() {
        assertThatThrownBy(() ->
                resolver.resolve(
                        null,
                        KnowledgeCategory.JPA
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "사용자는 필수입니다."
                );

        verify(knowledgeRepository, never())
                .findByUserIdAndParentIsNullAndName(
                        any(),
                        any()
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
                        .role(UserRole.USER)
                        .build();

        // when & then
        assertThatThrownBy(() ->
                resolver.resolve(
                        unsavedUser,
                        KnowledgeCategory.JPA
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "저장되지 않은 사용자는 Knowledge 카테고리를 생성할 수 없습니다."
                );

        verify(knowledgeRepository, never())
                .save(
                        any()
                );
    }

    @Test
    void 카테고리가_null이면_실패한다() {
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
                        "Knowledge 카테고리는 필수입니다."
                );

        verify(knowledgeRepository, never())
                .save(
                        any()
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