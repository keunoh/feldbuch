package io.github.kaltz.feldbuch.conversation;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationStatus;
import io.github.kaltz.feldbuch.conversation.entity.KnowledgeExtractStatus;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(io.github.kaltz.feldbuch.config.QueryDslConfig.class)
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private EntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .role(UserRole.USER)
                .build();

        em.persist(user);
    }

    @Test
    void 완료되고_지식_추출_상태가_NONE이면_배치_대상으로_조회한다() {
        // given
        Conversation conversation =
                createCompletedConversation("미처리 대화");

        em.flush();
        em.clear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(Conversation::getTitle)
                .containsExactly("미처리 대화");
    }

    @Test
    void 실패했고_재시도_횟수가_3회_미만이며_대기시간이_지났으면_조회한다() {
        // given
        Conversation conversation =
                createCompletedConversation("재시도 가능");

        conversation.failKnowledgeExtraction(
                "OpenAI 서버 오류"
        );

        em.flush();

        /**
         * 엔티티 메서드는 현재 시각을 저장하므로,
         * 재시도 대기시간이 지난 상황을 만들기 위해 DB 값을 직접 조정한다.
         */
        em.createNativeQuery("""
                        UPDATE conversations
                        SET knowledge_extract_failed_at = :failedAt
                        WHERE id = :conversationId
                        """)
                .setParameter(
                        "failedAt",
                        LocalDateTime.now()
                                .minusMinutes(2)
                )
                .setParameter(
                        "conversationId",
                        conversation.getId()
                )
                .executeUpdate();

        em.flush();
        em.clear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(Conversation::getTitle)
                .containsExactly("재시도 가능");
    }

    @Test
    void 실패_횟수가_3회면_배치_대상에서_제외한다() {
        // given
        Conversation conversation =
                createCompletedConversation("재시도 초과");

        conversation.failKnowledgeExtraction("첫 번째 실패");
        conversation.failKnowledgeExtraction("두 번째 실패");
        conversation.failKnowledgeExtraction("세 번째 실패");

        em.flush();

        em.createNativeQuery("""
                        UPDATE conversations
                        SET knowledge_extract_failed_at = :failedAt
                        WHERE id = :conversationId
                        """)
                .setParameter(
                        "failedAt",
                        LocalDateTime.now()
                                .minusMinutes(2)
                )
                .setParameter(
                        "conversationId",
                        conversation.getId()
                )
                .executeUpdate();

        em.flush();
        em.clear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(Conversation::getTitle)
                .doesNotContain("재시도 초과");
    }

    @Test
    void 실패했지만_재시도_대기시간이_지나지_않았으면_제외한다() {
        // given
        Conversation conversation =
                createCompletedConversation("대기 중");

        conversation.failKnowledgeExtraction(
                "일시적인 서버 오류"
        );

        em.flush();
        em.clear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(Conversation::getTitle)
                .doesNotContain("대기 중");
    }

    @Test
    void 대화가_ACTIVE이면_지식_추출_상태가_NONE이어도_제외한다() {
        // given
        Conversation conversation =
                Conversation.create(
                        user,
                        "진행 중인 대화"
                );

        em.persist(conversation);
        em.flush();
        em.clear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(Conversation::getTitle)
                .doesNotContain("진행 중인 대화");
    }

    @Test
    @DisplayName("지식 추출 대상 Conversation을 조회한다.")
    void findKnowledgeExtractionTargets() {

        // given
//        User user = User.builder()
//                .email("test@test.com")
//                .password("1234")
//                .nickname("tester")
//                .role(UserRole.USER)
//                .build();
//
//        em.persist(user);

        Conversation target =
                Conversation.create(
                        user,
                        "Spring"
                );

        target.complete();

        em.persist(target);

        Conversation active =
                Conversation.create(
                        user,
                        "Vue"
                );

        em.persist(active);

        Conversation extracted =
                Conversation.create(
                        user,
                        "JPA"
                );

        extracted.complete();
        extracted.completeKnowledgeExtraction();

        em.persist(extracted);

        em.flush();
        em.clear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .hasSize(1);

        assertThat(result.getFirst().getTitle())
                .isEqualTo("Spring");

        assertThat(result.getFirst().getStatus())
                .isEqualTo(
                        ConversationStatus.COMPLETED
                );

        assertThat(result.getFirst().getKnowledgeExtractStatus())
                .isEqualTo(
                        KnowledgeExtractStatus.NONE
                );
    }

    private Conversation createCompletedConversation(String title) {

        Conversation conversation =
                Conversation.create(user, title);

        conversation.complete();

        em.persist(conversation);

        return conversation;
    }
}
