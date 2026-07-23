package io.github.kaltz.feldbuch.conversation.entity;

import io.github.kaltz.feldbuch.user.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "conversation_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(nullable = false)
    private Integer sequence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationRole role;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Builder
    private ConversationMessage(
            Conversation conversation,
            Integer sequence,
            ConversationRole role,
            String content
    ) {
        this.conversation = conversation;
        this.sequence = sequence;
        this.role = role;
        this.content = content;
    }

    public static ConversationMessage create(
            Conversation conversation,
            Integer sequence,
            ConversationRole role,
            String content
    ) {
        return ConversationMessage.builder()
                .conversation(conversation)
                .sequence(sequence)
                .role(role)
                .content(content)
                .build();
    }
}
