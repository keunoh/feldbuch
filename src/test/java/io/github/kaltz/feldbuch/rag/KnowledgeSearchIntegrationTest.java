package io.github.kaltz.feldbuch.rag;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.service.KnowledgeNoteCommandService;
import io.github.kaltz.feldbuch.rag.service.KnowledgeSearchService;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "spring.batch.job.enabled=false",
                "rag.search.similarity-threshold=0.0"
        }
)
@ActiveProfiles("test")
class KnowledgeSearchIntegrationTest {

    @Autowired
    private UserRepository
            userRepository;

    @Autowired
    private ConversationRepository
            conversationRepository;

    @Autowired
    private KnowledgeNoteCommandService
            knowledgeNoteCommandService;

    @Autowired
    private KnowledgeSearchService
            knowledgeSearchService;

    private User userA;

    private User userB;

    private Conversation conversationA;

    private Conversation conversationB;

    @BeforeEach
    void setUp() {
        userA =
                createUser(
                        "user-a"
                );

        userB =
                createUser(
                        "user-b"
                );

        conversationA =
                createConversation(
                        userA,
                        "Spring 트랜잭션 학습"
                );

        conversationB =
                createConversation(
                        userB,
                        "Spring 트랜잭션 학습"
                );
    }

    @Test
    void 사용자의_지식만_검색한다() {

        // given
        AiKnowledgeSummaryResponse responseA =
                new AiKnowledgeSummaryResponse(
                        KnowledgeCategory.SPRING,
                        "Spring 트랜잭션",
                        "Spring 트랜잭션 처리 방식을 정리한 노트",
                        """
                                Spring에서는 @Transactional을 사용하여
                                선언적으로 트랜잭션을 관리할 수 있습니다.
                                """,
                        List.of(
                                "Spring",
                                "Transactional",
                                "Transaction"
                        )
                );

        AiKnowledgeSummaryResponse responseB =
                new AiKnowledgeSummaryResponse(
                        KnowledgeCategory.SPRING,
                        "Spring 트랜잭션 고급",
                        "다른 사용자의 Spring 트랜잭션 노트",
                        """
                                Spring 트랜잭션에서는 전파 속성과
                                격리 수준을 설정할 수 있습니다.
                                """,
                        List.of(
                                "Spring",
                                "Propagation",
                                "Isolation"
                        )
                );

        KnowledgeNote noteA =
                knowledgeNoteCommandService
                        .saveConsolidated(
                                userA,
                                conversationA,
                                responseA
                        );

        KnowledgeNote noteB =
                knowledgeNoteCommandService
                        .saveConsolidated(
                                userB,
                                conversationB,
                                responseB
                        );

        // when
        List<Document> results =
                knowledgeSearchService.search(
                        userA.getId(),
                        "Spring 트랜잭션은 어떻게 사용하는가?"
                );

        // then
        assertThat(results)
                .isNotEmpty();

        assertThat(results)
                .allSatisfy(document -> {

                    assertThat(
                            ((Number) document
                                    .getMetadata()
                                    .get("userId"))
                                    .longValue()
                    )
                            .isEqualTo(
                                    userA.getId()
                            );
                });

        assertThat(results)
                .anySatisfy(document -> {

                    assertThat(
                            ((Number) document
                                    .getMetadata()
                                    .get("knowledgeNoteId"))
                                    .longValue()
                    )
                            .isEqualTo(
                                    noteA.getId()
                            );
                });

        assertThat(results)
                .noneSatisfy(document -> {

                    assertThat(
                            ((Number) document
                                    .getMetadata()
                                    .get("knowledgeNoteId"))
                                    .longValue()
                    )
                            .isEqualTo(
                                    noteB.getId()
                            );
                });
    }

    private User createUser(
            String nickname
    ) {
        User user =
                User.builder()
                        .email(
                                nickname
                                        + "-"
                                        + System.nanoTime()
                                        + "@feldbuch.com"
                        )
                        .password(
                                "test-password"
                        )
                        .nickname(
                                nickname
                        )
                        .build();

        return userRepository
                .saveAndFlush(
                        user
                );
    }

    private Conversation createConversation(
            User user,
            String title
    ) {
        Conversation conversation =
                Conversation.create(
                        user,
                        title
                );

        return conversationRepository
                .saveAndFlush(
                        conversation
                );
    }
}