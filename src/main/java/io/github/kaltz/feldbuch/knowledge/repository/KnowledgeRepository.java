package io.github.kaltz.feldbuch.knowledge.repository;

import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {

    /**
     * 최상위 Knowledge 중 같은 이름이 있는지 조회
     */
    Optional<Knowledge> findByUserIdAndParentIsNullAndName(Long userId, String name);

    /**
     * 특정 상위 Knowledge 아래에서 같은 이름이 있는지 조회
     */
    Optional<Knowledge> findByUserIdAndParentIdAndName(Long userId, Long parentId, String name);

    List<Knowledge> findAllByUserIdOrderByCreatedAtAsc(Long userId);

}
