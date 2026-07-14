package io.github.kaltz.feldbuch.conversation.entity;

import io.github.kaltz.feldbuch.user.entity.BaseEntity;
import io.github.kaltz.feldbuch.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status;

    @Builder
    private Conversation(User user, String title, ConversationStatus status) {
        this.user = user;
        this.title = title;
        this.status = status;
    }

    public static Conversation create(User user, String title) {
        return Conversation.builder()
                .user(user)
                .title(title)
                .status(ConversationStatus.ACTIVE)
                .build();
    }

    public void complete() {
        this.status = ConversationStatus.COMPLETED;
    }

    public void changeTitle(String title) {
        this.title = title;
    }
}
