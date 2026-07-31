package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import io.github.kaltz.feldbuch.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KnowledgePathResolver {

    private final KnowledgeRepository knowledgeRepository;

    /**
     * Knowledge 경로를 순서대로 조회하고,
     * 존재하지 않는 경로는 자동으로 생성한다.
     * <p>
     * 예:
     * ["개발", "Spring", "JPA"]
     * <p>
     * 결과:
     * 개발
     * └── Spring
     * └── JPA
     * <p>
     * 최종 경로인 JPA Knowledge를 반환한다.
     */
    @Transactional
    public Knowledge resolve(User user, List<String> path) {
        validateUser(user);

        List<String> normalizedPath = normalizePath(path);

        Knowledge current = resolveRoot(
                user,
                normalizedPath.getFirst()
        );

        for (int index = 1; index < normalizedPath.size(); index++) {
            String name = normalizedPath.get(index);

            current = resolveChild(
                    user,
                    current,
                    name
            );
        }

        return current;
    }

    /**
     * 최상위 Knowledge를 조회하고,
     * 존재하지 않으면 새로 생성한다.
     */
    private Knowledge resolveRoot(User user, String name) {

        return knowledgeRepository
                .findByUserIdAndParentIsNullAndName(
                        user.getId(),
                        name
                )
                .orElseGet(() -> knowledgeRepository.save(
                        Knowledge.createRoot(
                                user, name
                        )
                ));
    }

    /**
     * 특정 Knowledge 아래의 자식 Knowledge를 조회하고,
     * 존재하지 않으면 새로 생성한다.
     */
    private Knowledge resolveChild(User user, Knowledge parent, String name) {

        return knowledgeRepository
                .findByUserIdAndParentIdAndName(
                        user.getId(),
                        parent.getId(),
                        name
                )
                .orElseGet(() -> knowledgeRepository.save(
                        Knowledge.createChild(
                                user,
                                parent,
                                name
                        )
                ));
    }

    /**
     * AI 응답에 빈 문자열이나 불필요한 공백이 포함될 수 있으므로
     * 저장 전에 경로를 정규화한다.
     */
    private List<String> normalizePath(List<String> path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException(
                    "Knowledge 경로는 필수입니다."
            );
        }

        List<String> normalizedPath = path.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .toList();

        if (normalizedPath.isEmpty()) {
            throw new IllegalArgumentException(
                    "Knowledge 경로에 유효한 이름이 없습니다."
            );
        }

        return normalizedPath;
    }

    private void validateUser(User user) {
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
}
