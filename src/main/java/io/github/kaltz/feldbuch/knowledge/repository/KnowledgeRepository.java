package io.github.kaltz.feldbuch.knowledge.repository;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {

    /**
     * 사용자의 최상위 Knowledge 목록 조회
     */
    List<Knowledge> findAllByUserIdAndParentIsNullOrderByNameAsc(Long userId);

    /**
     * 특정 Knowledge의 바로 아래 자식 목록 조회
     */
    List<Knowledge> findAllByUserIdAndParentIdOrderByNameAsc(Long userId, Long parentId);

    /**
     * 최상위 Knowledge 중 같은 이름이 있는지 조회
     */
    Optional<Knowledge> findByUserIdAndParentIsNullAndName(Long userId, String name);

    /**
     * 특정 상위 Knowledge 아래에서 같은 이름이 있는지 조회
     */
    Optional<Knowledge> findByUserIdAndParentIdAndName(Long userId, Long parentId, String name);

    List<Knowledge> findAllByUserIdOrderByCreatedAtAsc(Long userId);

    boolean existsByUserIdAndParentIsNullAndName(Long userId, String name);

    boolean existsByUserIdAndParentIdAndName(Long userId, Long parentId, String name);
}
