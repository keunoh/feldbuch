package io.github.kaltz.feldbuch.rag;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.knowledge.service.KnowledgeNoteCommandService;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.batch.job.enabled=false"})
@ActiveProfiles("test")
class KnowledgeVectorSyncIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private KnowledgeNoteRepository knowledgeNoteRepository;

    @Autowired
    private KnowledgeNoteCommandService knowledgeNoteCommandService;

    @Autowired
    private VectorStore vectorStore;

    private User user;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        user =
                User.builder()
                        .email(
                                "vector-integration-"
                                        + System.nanoTime()
                                        + "@feldbuch.com"
                        )
                        .password("test-password")
                        .nickname("vector-test")
                        .build();

        userRepository.saveAndFlush(user);

        conversation =
                Conversation.create(
                        user,
                        "Spring 트랜잭션 학습"
                );

        conversationRepository.saveAndFlush(conversation);
    }

    @Test
    void Consolidated_노트를_저장하면_VectorStore에_동기화된다() {

        // given
        AiKnowledgeSummaryResponse response =
                new AiKnowledgeSummaryResponse(
                        KnowledgeCategory.SPRING,
                        "Spring 트랜잭션",
                        "Spring의 트랜잭션 처리 방법을 정리한 노트",
                        """
                                Spring에서는 @Transactional 애노테이션을 사용하여
                                선언적으로 트랜잭션을 관리할 수 있습니다.
                                """,
                        List.of(
                                "Spring",
                                "Transaction",
                                "Transactional"
                        )
                );

        // when
        KnowledgeNote saved =
                knowledgeNoteCommandService
                        .saveConsolidated(
                                user,
                                conversation,
                                response
                        );

        // then
        assertThat(saved.getId())
                .isNotNull();

        KnowledgeNote found =
                knowledgeNoteRepository
                        .findById(saved.getId())
                        .orElseThrow();

        assertThat(found.isConsolidated())
                .isTrue();

        List<Document> results =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(
                                        "스프링에서 트랜잭션은 어떻게 사용하지?"
                                )
                                .filterExpression(
                                        "knowledgeNoteId == "
                                                + saved.getId()
                                )
                                .topK(3)
                                .build()
                );

        assertThat(results)
                .isNotEmpty();

        assertThat(results)
                .anySatisfy(document -> {

                    assertThat(
                            document.getText()
                    )
                            .contains(
                                    "Spring 트랜잭션"
                            )
                            .contains(
                                    "@Transactional"
                            );

                    assertThat(
                            ((Number) document
                                    .getMetadata()
                                    .get("knowledgeNoteId"))
                                    .longValue()
                    )
                            .isEqualTo(
                                    saved.getId()
                            );
                });
    }
}