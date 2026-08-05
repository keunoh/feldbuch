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
                ),
                @Index(
                        name = "idx_knowledge_note_conversation_type",
                        columnList = "conversation_id, note_type"
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
     * 노트의 역할
     * <p>
     * INCREMENTAL:
     * 새롭게 추출된 대화 범위마다 생성되는 개별 노트
     * <p>
     * CONSOLIDATED:
     * 같은 대화의 내용을 누적해서 관리하는 통합 노트
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "note_type",
            nullable = false,
            length = 20
    )
    private KnowledgeNoteType type =
            KnowledgeNoteType.INCREMENTAL;

    /**
     * AI가 생성한 학습 노트 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * AI가 생성한 한 줄 설명
     */
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    /**
     * 대화에서 추출한 학습 요약
     */
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String summary;

    /**
     * 노트 소유자
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_knowledge_note_user"
            )
    )
    private User user;

    /**
     * 이 노트가 생성된 원본 대화
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
    @Column(
            name = "keyword",
            nullable = false,
            length = 100
    )
    @Builder.Default
    private List<String> keywords =
            new ArrayList<>();

    /**
     * 새롭게 추출된 대화 범위의 개별 노트를 생성한다.
     */
    public static KnowledgeNote createIncremental(
            User user,
            Conversation conversation,
            Knowledge knowledge,
            String title,
            String description,
            String summary,
            List<String> keywords
    ) {
        return create(
                KnowledgeNoteType.INCREMENTAL,
                user,
                conversation,
                knowledge,
                title,
                description,
                summary,
                keywords
        );
    }

    /**
     * 같은 대화의 내용을 누적 관리하는 통합 노트를 생성한다.
     */
    public static KnowledgeNote createConsolidated(
            User user,
            Conversation conversation,
            Knowledge knowledge,
            String title,
            String description,
            String summary,
            List<String> keywords
    ) {
        return create(
                KnowledgeNoteType.CONSOLIDATED,
                user,
                conversation,
                knowledge,
                title,
                description,
                summary,
                keywords
        );
    }

    private static KnowledgeNote create(
            KnowledgeNoteType type,
            User user,
            Conversation conversation,
            Knowledge knowledge,
            String title,
            String description,
            String summary,
            List<String> keywords
    ) {
        validateType(type);
        validateUser(user);
        validateConversation(conversation);
        validateKnowledge(knowledge);
        validateTitle(title);
        validateDescription(description);
        validateSummary(summary);

        if (!knowledge.belongsTo(user)) {
            throw new IllegalArgumentException(
                    "Knowledge와 사용자가 일치하지 않습니다."
            );
        }

        return KnowledgeNote.builder()
                .type(type)
                .user(user)
                .conversation(conversation)
                .knowledge(knowledge)
                .title(title.trim())
                .description(description.trim())
                .summary(summary.trim())
                .keywords(
                        normalizeKeywords(
                                keywords
                        )
                )
                .build();
    }

    /**
     * 통합 노트의 내용을 갱신한다.
     */
    public void updateContent(
            String title,
            String description,
            String summary,
            List<String> keywords
    ) {
        validateTitle(title);
        validateDescription(description);
        validateSummary(summary);

        this.title = title.trim();
        this.description = description.trim();
        this.summary = summary.trim();

        this.keywords.clear();
        this.keywords.addAll(
                normalizeKeywords(
                        keywords
                )
        );
    }

    public void moveTo(
            Knowledge knowledge
    ) {
        validateKnowledge(knowledge);

        if (!knowledge.belongsTo(this.user)) {
            throw new IllegalArgumentException(
                    "다른 사용자의 Knowledge로 이동할 수 없습니다."
            );
        }

        this.knowledge = knowledge;
    }

    public boolean isIncremental() {
        return type == KnowledgeNoteType.INCREMENTAL;
    }

    public boolean isConsolidated() {
        return type == KnowledgeNoteType.CONSOLIDATED;
    }

    /**
     * 외부에서 컬렉션을 직접 수정하지 못하도록
     * 읽기 전용 목록으로 반환한다.
     */
    public List<String> getKeywords() {
        return Collections.unmodifiableList(
                keywords
        );
    }

    private static List<String> normalizeKeywords(
            List<String> keywords
    ) {
        if (
                keywords == null
                        || keywords.isEmpty()
        ) {
            return new ArrayList<>();
        }

        return keywords.stream()
                .filter(keyword ->
                        keyword != null
                                && !keyword.isBlank()
                )
                .map(String::trim)
                .filter(keyword ->
                        keyword.length() <= 100
                )
                .distinct()
                .limit(10)
                .collect(
                        java.util.stream.Collectors
                                .toCollection(
                                        ArrayList::new
                                )
                );
    }

    private static void validateType(
            KnowledgeNoteType type
    ) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "KnowledgeNote 유형은 필수입니다."
            );
        }
    }

    private static void validateUser(
            User user
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "사용자는 필수입니다."
            );
        }
    }

    private static void validateConversation(
            Conversation conversation
    ) {
        if (conversation == null) {
            throw new IllegalArgumentException(
                    "원본 대화는 필수입니다."
            );
        }
    }

    private static void validateKnowledge(
            Knowledge knowledge
    ) {
        if (knowledge == null) {
            throw new IllegalArgumentException(
                    "Knowledge는 필수입니다."
            );
        }
    }

    private static void validateTitle(
            String title
    ) {
        if (
                title == null
                        || title.isBlank()
        ) {
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

    private static void validateDescription(
            String description
    ) {
        if (
                description == null
                        || description.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "KnowledgeNote 설명은 필수입니다."
            );
        }

        if (description.trim().length() > 300) {
            throw new IllegalArgumentException(
                    "KnowledgeNote 설명은 300자를 초과할 수 없습니다."
            );
        }
    }

    private static void validateSummary(
            String summary
    ) {
        if (
                summary == null
                        || summary.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "KnowledgeNote 요약은 필수입니다."
            );
        }
    }
}
