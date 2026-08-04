package io.github.kaltz.feldbuch.conversation;

import io.github.kaltz.feldbuch.config.QueryDslConfig;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        QueryDslConfig.class,
        ConversationRepositoryTest.FixedClockConfig.class
})
class ConversationRepositoryTest {

    private static final LocalDateTime CURRENT_TIME =
            LocalDateTime.of(
                    2026,
                    8,
                    2,
                    12,
                    0
            );

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private EntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        user =
                User.builder()
                        .email("test@test.com")
                        .password("password")
                        .nickname("tester")
                        .role(UserRole.USER)
                        .build();

        em.persist(user);
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        public Clock clock() {
            return Clock.fixed(
                    Instant.parse(
                            "2026-08-02T03:00:00Z"
                    ),
                    ZoneId.of("Asia/Seoul")
            );
        }
    }

    @Test
    void 완료되고_지식_추출_상태가_NONE이면_배치_대상으로_조회한다() {
        // given
        createCompletedConversation(
                "미처리 대화"
        );

        flushAndClear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(
                        Conversation::getTitle
                )
                .containsExactly(
                        "미처리 대화"
                );
    }

    @Test
    void 실패했고_재시도_횟수가_3회_미만이며_대기시간이_지났으면_조회한다() {
        // given
        Conversation conversation =
                createCompletedConversation(
                        "재시도 가능"
                );

        conversation.failKnowledgeExtraction(
                "OpenAI 서버 오류",
                CURRENT_TIME.minusMinutes(2)
        );

        flushAndClear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(
                        Conversation::getTitle
                )
                .containsExactly(
                        "재시도 가능"
                );
    }

    @Test
    void 실패_횟수가_3회면_배치_대상에서_제외한다() {
        // given
        Conversation conversation =
                createCompletedConversation(
                        "재시도 초과"
                );

        LocalDateTime failedAt =
                CURRENT_TIME.minusMinutes(2);

        conversation.failKnowledgeExtraction(
                "첫 번째 실패",
                failedAt
        );

        conversation.failKnowledgeExtraction(
                "두 번째 실패",
                failedAt
        );

        conversation.failKnowledgeExtraction(
                "세 번째 실패",
                failedAt
        );

        flushAndClear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(
                        Conversation::getTitle
                )
                .doesNotContain(
                        "재시도 초과"
                );
    }

    @Test
    void 실패했지만_재시도_대기시간이_지나지_않았으면_제외한다() {
        // given
        Conversation conversation =
                createCompletedConversation(
                        "대기 중"
                );

        conversation.failKnowledgeExtraction(
                "일시적인 서버 오류",
                CURRENT_TIME
        );

        flushAndClear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(
                        Conversation::getTitle
                )
                .doesNotContain(
                        "대기 중"
                );
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

        flushAndClear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(
                        Conversation::getTitle
                )
                .doesNotContain(
                        "진행 중인 대화"
                );
    }

    @Test
    void 이미_지식_추출이_완료된_대화는_제외한다() {
        // given
        Conversation extracted =
                createCompletedConversation(
                        "추출 완료"
                );

        extracted.completeKnowledgeExtraction(
                10L
        );

        flushAndClear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .extracting(
                        Conversation::getTitle
                )
                .doesNotContain(
                        "추출 완료"
                );
    }

    @Test
    void 지식_추출_대상이_존재하면_true를_반환한다() {
        // given
        createCompletedConversation(
                "추출 대상"
        );

        flushAndClear();

        // when
        boolean result =
                conversationRepository
                        .existsKnowledgeExtractionTarget();

        // then
        assertThat(result)
                .isTrue();
    }

    @Test
    void 지식_추출_대상이_없으면_false를_반환한다() {
        // given
        Conversation active =
                Conversation.create(
                        user,
                        "진행 중"
                );

        em.persist(active);

        Conversation extracted =
                createCompletedConversation(
                        "이미 추출됨"
                );

        extracted.completeKnowledgeExtraction(
                20L
        );

        flushAndClear();

        // when
        boolean result =
                conversationRepository
                        .existsKnowledgeExtractionTarget();

        // then
        assertThat(result)
                .isFalse();
    }

    @Test
    @DisplayName("지식 추출 대상 Conversation을 조회한다.")
    void findKnowledgeExtractionTargets() {
        // given
        Conversation target =
                createCompletedConversation(
                        "Spring"
                );

        Conversation active =
                Conversation.create(
                        user,
                        "Vue"
                );

        em.persist(active);

        Conversation extracted =
                createCompletedConversation(
                        "JPA"
                );

        extracted.completeKnowledgeExtraction(
                100L
        );

        flushAndClear();

        // when
        List<Conversation> result =
                conversationRepository
                        .findKnowledgeExtractionTargets();

        // then
        assertThat(result)
                .hasSize(1);

        Conversation found =
                result.getFirst();

        assertThat(found.getId())
                .isEqualTo(
                        target.getId()
                );

        assertThat(found.getTitle())
                .isEqualTo(
                        "Spring"
                );

        assertThat(found.getStatus())
                .isEqualTo(
                        ConversationStatus.COMPLETED
                );

        assertThat(found.getKnowledgeExtractStatus())
                .isEqualTo(
                        KnowledgeExtractStatus.NONE
                );
    }

    @Test
    void 마지막_메시지_이후_기준_시간이_지난_ACTIVE_대화를_조회한다() {
        // given
        Conversation inactive =
                Conversation.create(
                        user,
                        "오래된 대화"
                );

        inactive.recordMessageActivity(
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        11,
                        0
                )
        );

        em.persist(inactive);

        Conversation recent =
                Conversation.create(
                        user,
                        "최근 대화"
                );

        recent.recordMessageActivity(
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        11,
                        50
                )
        );

        em.persist(recent);

        Conversation completed =
                Conversation.create(
                        user,
                        "이미 완료된 대화"
                );

        completed.recordMessageActivity(
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        10,
                        0
                )
        );

        completed.complete();

        em.persist(completed);

        em.flush();
        em.clear();

        LocalDateTime cutoff =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        11,
                        30
                );

        // when
        List<Conversation> result =
                conversationRepository
                        .findInactiveActiveConversations(
                                cutoff
                        );

        // then
        assertThat(result)
                .extracting(
                        Conversation::getTitle
                )
                .containsExactly(
                        "오래된 대화"
                );
    }

    private Conversation createCompletedConversation(
            String title
    ) {
        Conversation conversation =
                Conversation.create(
                        user,
                        title
                );

        conversation.complete();

        em.persist(conversation);

        return conversation;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}