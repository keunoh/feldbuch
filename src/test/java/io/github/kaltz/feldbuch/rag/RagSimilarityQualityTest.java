package io.github.kaltz.feldbuch.rag;

import io.github.kaltz.feldbuch.ai.dto.AiKnowledgeSummaryResponse;
import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.service.KnowledgeNoteCommandService;
import io.github.kaltz.feldbuch.rag.service.KnowledgeSearchService;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class RagSimilarityQualityTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private KnowledgeNoteCommandService knowledgeNoteCommandService;

    @Autowired
    private KnowledgeSearchService knowledgeSearchService;

    private User user;

    @BeforeEach
    void setUp() {

        user =
                createUser(
                        "similarity-test"
                );

        Conversation conversation =
                createConversation(
                        user,
                        "Spring 트랜잭션 학습"
                );

        AiKnowledgeSummaryResponse response =
                new AiKnowledgeSummaryResponse(
                        KnowledgeCategory.SPRING,
                        "Spring 트랜잭션",
                        "Spring 트랜잭션 사용 방법을 정리한 노트",
                        """
                                Spring에서는 @Transactional을 사용하여
                                선언적으로 트랜잭션을 관리할 수 있습니다.
                                트랜잭션 전파 속성과 격리 수준도 설정할 수 있습니다.
                                """,
                        List.of(
                                "Spring",
                                "Transactional",
                                "Transaction"
                        )
                );

        knowledgeNoteCommandService
                .saveConsolidated(
                        user,
                        conversation,
                        response
                );
    }

    @Test
    void 관련_질문과_무관한_질문의_similarity_score를_비교한다() {

        // given
        List<String> relatedQueries =
                List.of(
                        "@Transactional은 어떻게 사용해?",
                        "Spring 트랜잭션 전파 속성이 뭐야?",
                        "트랜잭션 격리 수준을 알려줘",
                        "Spring에서 트랜잭션을 관리하는 방법은?"
                );

        List<String> unrelatedQueries =
                List.of(
                        "오늘 저녁 메뉴 추천해줘",
                        "Docker 이미지 만드는 방법 알려줘",
                        "Vue computed가 뭐야?",
                        "AWS EC2 비용은 얼마야?"
                );

        // when
        List<Double> relatedScores =
                searchScores(
                        relatedQueries
                );

        List<Double> unrelatedScores =
                searchScores(
                        unrelatedQueries
                );

        // then
        printScores(
                "관련 질문",
                relatedQueries,
                relatedScores
        );

        printScores(
                "무관 질문",
                unrelatedQueries,
                unrelatedScores
        );

        printSummary(
                relatedScores,
                unrelatedScores
        );
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

    private List<Double> searchScores(List<String> queries) {

        return queries.stream()
                .map(query ->
                        knowledgeSearchService.search(
                                user.getId(),
                                query
                        )
                )
                .map(results -> {

                    assertThat(results)
                            .isNotEmpty();

                    return results
                            .getFirst()
                            .getScore();
                })
                .toList();
    }

    private void printScores(String group, List<String> queries, List<Double> scores) {

        System.out.printf("%n[%s]%n", group);

        for (int i = 0; i < queries.size(); i++) {

            System.out.printf("%.4f | %s%n", scores.get(i), queries.get(i));
        }
    }

    private void printSummary(List<Double> relatedScores, List<Double> unrelatedScores) {

        double relatedMin =
                relatedScores.stream()
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElseThrow();

        double unrelatedMax =
                unrelatedScores.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElseThrow();

        System.out.printf(
                """
                        
                        [Similarity Summary]
                        related min   : %.4f
                        unrelated max : %.4f
                        gap           : %.4f
                        %n""",
                relatedMin,
                unrelatedMax,
                relatedMin - unrelatedMax
        );
    }
}