package io.github.kaltz.feldbuch.conversation;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.conversation.entity.ConversationStatus;
import io.github.kaltz.feldbuch.conversation.entity.KnowledgeExtractStatus;
import io.github.kaltz.feldbuch.conversation.repository.ConversationRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import io.github.kaltz.feldbuch.user.entity.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(io.github.kaltz.feldbuch.config.QueryDslConfig.class)
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("지식 추출 대상 Conversation을 조회한다.")
    void findKnowledgeExtractionTargets() {

        // given
        User user = User.builder()
                .email("test@test.com")
                .password("1234")
                .nickname("tester")
                .role(UserRole.USER)
                .build();

        em.persist(user);

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
}
