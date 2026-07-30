package io.github.kaltz.feldbuch.knowledge.entity;

import io.github.kaltz.feldbuch.user.entity.BaseEntity;
import io.github.kaltz.feldbuch.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "knowledge",
        indexes = {
                @Index(
                        name = "idx_knowledge_user_parent",
                        columnList = "user_id, parent_id"
                ),
                @Index(
                        name = "idx_knowledge_parent",
                        columnList = "parent_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class Knowledge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 화면에 표시할 폴더 이름
     * <p>
     * 예:
     * 개발, Spring, Vue, 취업
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 폴더 소유자
     * <p>
     * 사용자마다 서로 다른 지식 폴더 구조를 가진다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_knowledge_user")
    )
    private User user;

    /**
     * 상위 폴더
     * <p>
     * null이면 최상위 폴더다.
     * <p>
     * 예:
     * 개발          parent = null
     * Spring       parent = 개발
     * Spring Batch parent = Spring
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id",
            foreignKey = @ForeignKey(name = "fk_knowledge_parent")
    )
    private Knowledge parent;

    /**
     * 최상위 Knowledge 생성
     */
    public static Knowledge createRoot(
            User user,
            String name
    ) {
        validateUser(user);
        validateName(name);

        return Knowledge.builder()
                .user(user)
                .name(name)
                .parent(null)
                .build();
    }

    /**
     * 하위 Knowledge 생성
     */
    public static Knowledge createChild(
            User user,
            Knowledge parent,
            String name
    ) {
        validateUser(user);
        validateParent(parent);
        validateName(name);

        if (!parent.belongsTo(user)) {
            throw new IllegalArgumentException(
                    "상위 Knowledge와 사용자가 일치하지 않습니다."
            );
        }

        return Knowledge.builder()
                .user(user)
                .name(name.trim())
                .parent(parent)
                .build();
    }

    public void rename(String name) {
        validateName(name);

        this.name = name.trim();
    }

    public void moveTo(Knowledge parent) {
        if (parent == this) {
            throw new IllegalArgumentException(
                    "Knowledge 자신을 상위 폴더로 지정할 수 없습니다."
            );
        }

        if (parent != null && !parent.belongsTo(this.user)) {
            throw new IllegalArgumentException(
                    "다른 사용자의 Knowledge로 이동할 수 없습니다."
            );
        }

        this.parent = parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean belongsTo(User user) {
        if (user == null || this.user == null) {
            return false;
        }

        return this.user.getId() != null
                && this.user.getId().equals(user.getId());
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "사용자는 필수입니다."
            );
        }
    }

    private static void validateParent(Knowledge parent) {
        if (parent == null) {
            throw new IllegalArgumentException(
                    "상위 Knowledge는 필수입니다."
            );
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Knowledge 이름은 필수입니다."
            );
        }

        if (name.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Knowledge 이름은 100자를 초과할 수 없습니다."
            );
        }
    }

}
