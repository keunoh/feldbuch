package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class KnowledgeCategoryResolver {

    private final KnowledgeRepository knowledgeRepository;

    /**
     * 고정된 KnowledgeCategory를 실제 Knowledge 폴더로 변환한다.
     * <p>
     * 폴더 구조는 항상 다음 두 단계로 제한된다.
     * <p>
     * 대분류
     * └── 세부 카테고리
     * <p>
     * 예:
     * DATABASE
     * └── JPA
     * <p>
     * WEB_DEVELOPMENT
     * └── Spring Batch
     * <p>
     * 최종 세부 카테고리 Knowledge를 반환한다.
     */
    @Transactional
    public Knowledge resolve(
            User user,
            KnowledgeCategory category
    ) {
        validateUser(user);
        validateCategory(category);

        Knowledge root =
                resolveRoot(
                        user,
                        category.getRootCategory()
                );

        return resolveCategory(
                user,
                root,
                category
        );
    }

    /**
     * 사용자의 대분류 폴더를 조회하고,
     * 존재하지 않으면 enum 이름으로 생성한다.
     * <p>
     * 예:
     * WEB_DEVELOPMENT
     * DATABASE
     * DEVOPS
     */
    private Knowledge resolveRoot(
            User user,
            KnowledgeRootCategory rootCategory
    ) {
        String rootName =
                rootCategory.name();

        return knowledgeRepository
                .findByUserIdAndParentIsNullAndName(
                        user.getId(),
                        rootName
                )
                .orElseGet(() ->
                        knowledgeRepository.save(
                                Knowledge.createRoot(
                                        user,
                                        rootName
                                )
                        )
                );
    }

    /**
     * 대분류 바로 아래에 고정 카테고리 폴더를 조회하거나 생성한다.
     * <p>
     * AI가 임의의 폴더명을 생성하지 않으며,
     * KnowledgeCategory에 정의된 이름만 사용한다.
     */
    private Knowledge resolveCategory(
            User user,
            Knowledge root,
            KnowledgeCategory category
    ) {
        String categoryName =
                category.getDisplayName();

        return knowledgeRepository
                .findByUserIdAndParentIdAndName(
                        user.getId(),
                        root.getId(),
                        categoryName
                )
                .orElseGet(() ->
                        knowledgeRepository.save(
                                Knowledge.createChild(
                                        user,
                                        root,
                                        categoryName
                                )
                        )
                );
    }

    private void validateUser(
            User user
    ) {
        if (user == null) {
            throw new IllegalArgumentException(
                    "사용자는 필수입니다."
            );
        }

        if (user.getId() == null) {
            throw new IllegalArgumentException(
                    "저장되지 않은 사용자는 Knowledge 카테고리를 생성할 수 없습니다."
            );
        }
    }

    private void validateCategory(
            KnowledgeCategory category
    ) {
        if (category == null) {
            throw new IllegalArgumentException(
                    "Knowledge 카테고리는 필수입니다."
            );
        }

        if (category.getRootCategory() == null) {
            throw new IllegalArgumentException(
                    "Knowledge 대분류는 필수입니다."
            );
        }

        if (
                category.getDisplayName() == null
                        || category.getDisplayName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Knowledge 카테고리 이름은 필수입니다."
            );
        }
    }
}