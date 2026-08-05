package io.github.kaltz.feldbuch.knowledge.folder;

public record AiKnowledgeFolderSelectionResponse(
        AiKnowledgeFolderSelectionType selectionType,
        Long selectedKnowledgeId
) {
}
