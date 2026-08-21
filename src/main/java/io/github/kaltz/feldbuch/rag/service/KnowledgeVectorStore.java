package io.github.kaltz.feldbuch.rag.service;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.rag.config.RagSearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KnowledgeVectorStore {

    /**
     * Spring AI 검색 방법
     * userId metadata filter
     */
    private final VectorStore vectorStore;
    private final RagSearchProperties ragSearchProperties;

    public void save(KnowledgeNote note) {

        validateNote(note);

        String documentId = documentId(note.getId());

        Document document =
                new Document(
                        documentId,
                        buildContent(note),
                        Map.of(
                                "type",
                                "knowledge",

                                "knowledgeNoteId",
                                note.getId(),

                                "userId",
                                note.getUser().getId(),

                                "conversationId",
                                note.getConversation().getId(),

                                "knowledgeId",
                                note.getKnowledge().getId(),

                                "noteType",
                                note.getType().name()
                        )
                );

        vectorStore.delete(List.of(documentId));

        vectorStore.add(List.of(document));
    }

    public List<Document> search(Long userId, String query) {

        SearchRequest request =
                SearchRequest.builder()
                        .query(query)
                        .topK(
                                ragSearchProperties.getTopK()
                        )
                        .similarityThreshold(
                                ragSearchProperties.getSimilarityThreshold()
                        )
                        .filterExpression(
                                "userId == " + userId
                        )
                        .build();

        return vectorStore.similaritySearch(request);
    }

    private String documentId(Long knowledgeNoteId) {

        return UUID.nameUUIDFromBytes(
                ("knowledge-note-" + knowledgeNoteId).getBytes(StandardCharsets.UTF_8)
        ).toString();
    }

    private String buildContent(KnowledgeNote note) {

        return """
                제목: %s
                설명: %s
                내용:
                %s
                키워드: %s
                """.formatted(
                note.getTitle(),
                note.getDescription(),
                note.getSummary(),
                String.join(
                        ", ",
                        note.getKeywords()
                )
        );
    }

    private void validateNote(KnowledgeNote note) {

        if (note == null) {
            throw new IllegalArgumentException(
                    "KnowledgeNote는 필수입니다."
            );
        }

        if (note.getId() == null) {
            throw new IllegalArgumentException(
                    "저장된 KnowledgeNote만 벡터화할 수 있습니다."
            );
        }

        if (!note.isConsolidated()) {
            throw new IllegalArgumentException(
                    "통합 노트만 벡터화할 수 있습니다."
            );
        }
    }
}
