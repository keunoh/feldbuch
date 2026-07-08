package io.github.kaltz.feldbuch.note.entity;

import io.github.kaltz.feldbuch.user.entity.BaseEntity;
import io.github.kaltz.feldbuch.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class Note extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NoteCategory category;

    @Column(nullable = false)
    private boolean pinned;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_note_user")
    )
    private User user;

    // 객체 생성 방식이 하나로 통일된다.
    public static Note create(
            User user,
            String title,
            String content,
            NoteCategory category
    ) {
        return Note.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(category == null ? NoteCategory.MEMO : category)
                .summary(null)
                .pinned(false)
                .build();
    }

    public void update(
            String title,
            String content,
            NoteCategory category
    ) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void pin() {
        this.pinned = true;
    }

    public void unpin() {
        this.pinned = false;
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}
