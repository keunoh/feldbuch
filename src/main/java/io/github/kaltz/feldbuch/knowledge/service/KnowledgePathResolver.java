package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;
import io.github.kaltz.feldbuch.knowledge.folder.AiKnowledgeFolderSelectionResponse;
import io.github.kaltz.feldbuch.knowledge.folder.AiKnowledgeFolderSelectionType;
import io.github.kaltz.feldbuch.knowledge.folder.KnowledgeFolderCandidate;
import io.github.kaltz.feldbuch.knowledge.folder.KnowledgeFolderSelectionService;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
// AI가 준 경로를 실제 Knowledge 트리로 변환해 주는 클래스이다.
public class KnowledgePathResolver {

    private static final int MAX_CHILD_PATH_DEPTH = 2;

    private final KnowledgeRepository knowledgeRepository;

    private final KnowledgeFolderSelectionService
            folderSelectionService;

    /**
     * 고정 대분류와 AI가 생성한 하위 경로를
     * 실제 Knowledge 트리로 변환한다.
     */
    @Transactional
    public Knowledge resolve(
            User user,
            KnowledgeRootCategory rootCategory,
            List<String> childPath
    ) {
        validateUser(user);
        validateRootCategory(rootCategory);

        List<String> normalizedChildPath =
                normalizeChildPath(
                        childPath
                );

        Knowledge current =
                resolveRoot(
                        user,
                        rootCategory
                );

        for (String requestedFolderName
                : normalizedChildPath) {

            current =
                    resolveChild(
                            user,
                            rootCategory,
                            current,
                            requestedFolderName
                    );
        }

        return current;
    }

    private Knowledge resolveRoot(
            User user,
            KnowledgeRootCategory rootCategory
    ) {
        String rootName =
                rootCategory.getDisplayName();

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

    private Knowledge resolveChild(
            User user,
            KnowledgeRootCategory rootCategory,
            Knowledge parent,
            String requestedFolderName
    ) {
        /*
         * 동일 이름이 존재하면 AI 호출 없이 즉시 재사용한다.
         */
        return knowledgeRepository
                .findByUserIdAndParentIdAndName(
                        user.getId(),
                        parent.getId(),
                        requestedFolderName
                )
                .orElseGet(() ->
                        selectOrCreateChild(
                                user,
                                rootCategory,
                                parent,
                                requestedFolderName
                        )
                );
    }


    private Knowledge selectOrCreateChild(
            User user,
            KnowledgeRootCategory rootCategory,
            Knowledge parent,
            String requestedFolderName
    ) {
        List<Knowledge> existingChildren =
                knowledgeRepository
                        .findAllByUserIdAndParentIdOrderByNameAsc(
                                user.getId(),
                                parent.getId()
                        );

        List<KnowledgeFolderCandidate> candidates =
                existingChildren.stream()
                        .map(
                                KnowledgeFolderCandidate::from
                        )
                        .toList();

        AiKnowledgeFolderSelectionResponse selection =
                folderSelectionService.select(
                        rootCategory.getDisplayName(),
                        parent.getName(),
                        requestedFolderName,
                        candidates
                );

        if (
                selection.selectionType()
                        == AiKnowledgeFolderSelectionType.EXISTING
        ) {
            return findSelectedKnowledge(
                    user,
                    selection.selectedKnowledgeId()
            );
        }

        return knowledgeRepository.save(
                Knowledge.createChild(
                        user,
                        parent,
                        requestedFolderName
                )
        );
    }

    private Knowledge findSelectedKnowledge(
            User user,
            Long selectedKnowledgeId
    ) {
        return knowledgeRepository
                .findByIdAndUserId(
                        selectedKnowledgeId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "AI가 선택한 Knowledge를 찾을 수 없습니다. knowledgeId="
                                        + selectedKnowledgeId
                        )
                );
    }

    private List<String> normalizeChildPath(
            List<String> childPath
    ) {
        if (
                childPath == null
                        || childPath.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "Knowledge 하위 경로는 필수입니다."
            );
        }

        List<String> normalizedPath =
                childPath.stream()
                        .filter(name ->
                                name != null
                                        && !name.isBlank()
                        )
                        .map(String::trim)
                        .distinct()
                        .toList();

        if (normalizedPath.isEmpty()) {
            throw new IllegalArgumentException(
                    "Knowledge 하위 경로에 유효한 이름이 없습니다."
            );
        }

        if (
                normalizedPath.size()
                        > MAX_CHILD_PATH_DEPTH
        ) {
            throw new IllegalArgumentException(
                    "Knowledge 하위 경로는 최대 "
                            + MAX_CHILD_PATH_DEPTH
                            + "단계까지 허용됩니다."
            );
        }

        return normalizedPath;
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
                    "저장되지 않은 사용자는 Knowledge 경로를 생성할 수 없습니다."
            );
        }
    }

    private void validateRootCategory(
            KnowledgeRootCategory rootCategory
    ) {
        if (rootCategory == null) {
            throw new IllegalArgumentException(
                    "Knowledge 대분류는 필수입니다."
            );
        }
    }
}
