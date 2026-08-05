package io.github.kaltz.feldbuch.knowledge.service;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private Knowledge knowledge;
    private AiKnowledgeSummaryResponse response;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .role(UserRole.USER)
                .build();

        ReflectionTestUtils.setField(user, "id", 1L);

        conversation = Conversation.create(user, "Spring");

        knowledge = Knowledge.createRoot(user, "Spring");

        response = new AiKnowledgeSummaryResponse(
                KnowledgeRootCategory.WEB_DEVELOPMENT,
                List.of("Backend", "Spring"),
                "Spring",
                "JPA",
                "JPA 학습 내용",
                List.of("Spring", "JPA")
        );
    }

    @Test
    void AI_요약용_KnowledgeNote로_저장한다() {

        // given
        when(knowledgePathResolver.resolve(
                        user,
                        response.rootCategory(),
                        response.knowledgePath()
                )
        )
                .thenReturn(knowledge);

        when(knowledgeNoteRepository.save(any(KnowledgeNote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        KnowledgeNote result =
                knowledgeNoteCommandService.saveAiSummary(
                        user,
                        conversation,
                        response
                );

        // then
        ArgumentCaptor<KnowledgeNote> captor =
                ArgumentCaptor.forClass(KnowledgeNote.class);

        verify(knowledgePathResolver)
                .resolve(
                        user,
                        response.rootCategory(),
                        response.knowledgePath()
                );

        verify(knowledgeNoteRepository)
                .save(captor.capture());

        KnowledgeNote saved = captor.getValue();

        assertThat(saved.getKnowledge()).isEqualTo(knowledge);
        assertThat(saved.getTitle()).isEqualTo(response.title());
        assertThat(saved.getDescription()).isEqualTo(response.description());
        assertThat(saved.getSummary()).isEqualTo(response.summary());
        assertThat(saved.getKeywords())
                .containsExactlyElementsOf(response.keywords());

        assertThat(result).isSameAs(saved);
    }
}