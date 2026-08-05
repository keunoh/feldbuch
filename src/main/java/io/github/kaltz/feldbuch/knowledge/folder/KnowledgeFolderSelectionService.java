package io.github.kaltz.feldbuch.knowledge.folder;

import java.util.List;

public interface KnowledgeFolderSelectionService {

    AiKnowledgeFolderSelectionResponse select(
            String rootCategoryName,
            String parentFolderName,
            String requestedFolderName,
            List<KnowledgeFolderCandidate> candidates
    );
}
