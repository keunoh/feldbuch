package io.github.kaltz.feldbuch.knowledge.entity;

import io.github.kaltz.feldbuch.conversation.entity.Conversation;
import io.github.kaltz.feldbuch.user.entity.BaseEntity;
import io.github.kaltz.feldbuch.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(
        name = "knowledge_notes",
        indexes = {
                @Index(
                        name = "idx_knowledge_note_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_knowledge_note_knowledge",
                        columnList = "knowledge_id"
                ),
                @Index(
                        name = "idx_knowledge_note_conversation",
                        columnList = "conversation_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class KnowledgeNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * AI가 생성한 학습 노트 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 대화에서 추출한 짧은 학습 요약
     */
    @Lob
    @Column(nullable = false)
    private String summary;

    /**
     * 노트 소유자
     * <p>
     * Conversation과 Knowledge를 통해서도 사용자를 알 수 있지만,
     * 사용자별 조회를 단순하고 빠르게 만들기 위해 직접 연결한다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_knowledge_note_user")
    )
    private User user;

    /**
     * 이 노트가 생성된 원본 대화
     * <p>
     * 한 대화에서 주제가 여러 개 발견되면
     * 여러 KnowledgeNote가 만들어질 수 있으므로 ManyToOne이다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_knowledge_note_conversation"
            )
    )
    private Conversation conversation;

    /**
     * 노트가 저장될 지식 폴더
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "knowledge_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_knowledge_note_knowledge"
            )
    )
    private Knowledge knowledge;

    /**
     * AI가 추출한 검색 및 복습용 키워드
     * <p>
     * 별도 테이블 Knowledge_note_keywords로 저장된다.
     */
    @ElementCollection
    @CollectionTable(
            name = "knowledge_note_keywords",
            joinColumns = @JoinColumn(
                    name = "knowledge_note_id",
                    foreignKey = @ForeignKey(
                            name = "fk_knowledge_note_keyword_note"
                    )
            )
    )
    @Column(name = "keyword", nullable = false, length = 100)
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    public static KnowledgeNote create(
            User user,
            Conversation conversation,
            Knowledge knowledge,
            String title,
            String summary,
            List<String> keywords
    ) {
        validateUser(user);
        validateConversation(conversation);
        validateKnowledge(knowledge);
        validateTitle(title);
        validateSummary(summary);

        if (!knowledge.belongsTo(user)) {
            throw new IllegalArgumentException(
                    "Knowledge와 사용자가 일치하지 않습니다."
            );
        }

        return KnowledgeNote.builder()
                .user(user)
                .conversation(conversation)
                .knowledge(knowledge)
                .title(title)
                .summary(summary)
                .keywords(normalizeKeywords(keywords))
                .build();
    }

    public void updateContent(
            String title,
            String summary,
            List<String> keywords
    ) {
        validateTitle(title);
        validateSummary(summary);

        this.title = title;
        this.summary = summary;
        this.keywords.clear();
        this.keywords.addAll(normalizeKeywords(keywords));
    }

    public void moveTo(Knowledge knowledge) {
        validateKnowledge(knowledge);

        if (!knowledge.belongsTo(this.user)) {
            throw new IllegalArgumentException(
                    "다른 사용자의 Knowledge로 이동할 수 없습니다."
            );
        }

        this.knowledge = knowledge;
    }

    /**
     * 외부에서 컬렉션을 직접 수정하지 못하도록 읽기 전용으로 반환한다.
     */
    public List<String> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }

    private static List<String> normalizeKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return new ArrayList<>();
        }

        return keywords.stream()
                .filter(keyword -> keyword != null && !keyword.isBlank())
                .map(String::trim)
                .filter(keyword -> keyword.length() <= 100)
                .distinct()
                .limit(10)
                .collect(
                        java.util.stream.Collectors.toCollection(
                                ArrayList::new
                        )
                );
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "사용자는 필수입니다."
            );
        }
    }

    private static void validateConversation(Conversation conversation) {
        if (conversation == null) {
            throw new IllegalArgumentException(
                    "원본 대화는 필수입니다."
            );
        }
    }

    private static void validateKnowledge(Knowledge knowledge) {
        if (knowledge == null) {
            throw new IllegalArgumentException(
                    "Knowledge는 필수입니다."
            );
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "KnowledgeNote 제목은 필수입니다."
            );
        }

        if (title.trim().length() > 200) {
            throw new IllegalArgumentException(
                    "KnowledgeNote 제목은 200자를 초과할 수 없습니다."
            );
        }
    }

    private static void validateSummary(String summary) {
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException(
                    "KnowledgeNote 요약은 필수입니다."
            );
        }
    }

}
