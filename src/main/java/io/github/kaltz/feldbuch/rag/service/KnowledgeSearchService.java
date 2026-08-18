package io.github.kaltz.feldbuch.rag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeSearchService {

    /**
     * 검색 유스케이스(Search Use Case)
     * <p>
     * 사용자가 원하는 정보를 검색하는 과정을 하나의 기능 단위로 정의한 것입니다.
     */

    private final KnowledgeVectorStore knowledgeVectorStore;

    public List<Document> search(Long userId, String query) {

        validateUserId(userId);

        validateQuery(query);

        return knowledgeVectorStore.search(userId, query);
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "사용자 ID는 필수입니다."
            );
        }
    }

    private void validateQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "검색어는 필수입니다."
            );
        }
    }
}
