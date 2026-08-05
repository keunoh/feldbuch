package io.github.kaltz.feldbuch.knowledge.service;

import io.github.kaltz.feldbuch.common.exception.CustomException;
import io.github.kaltz.feldbuch.common.exception.ErrorCode;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeNoteDetailResponse;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeNoteSummaryResponse;
import io.github.kaltz.feldbuch.knowledge.dto.response.KnowledgeTreeResponse;
import io.github.kaltz.feldbuch.knowledge.entity.Knowledge;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNoteType;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeNoteRepository;
import io.github.kaltz.feldbuch.knowledge.repository.KnowledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeQueryService {

    private final KnowledgeRepository knowledgeRepository;

    private final KnowledgeNoteRepository knowledgeNoteRepository;

    public List<KnowledgeTreeResponse> findTree(
            Long userId
    ) {
        List<Knowledge> knowledgeList =
                knowledgeRepository
                        .findAllByUserIdOrderByCreatedAtAsc(
                                userId
                        );

        return buildTree(
                knowledgeList
        );
    }

    public KnowledgeNoteDetailResponse findNote(
            Long userId,
            Long noteId
    ) {
        KnowledgeNote note =
                knowledgeNoteRepository
                        .findByIdAndUserId(
                                noteId,
                                userId
                        )
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.KNOWLEDGE_NOTE_NOT_FOUND
                                )
                        );

        return KnowledgeNoteDetailResponse.from(
                note
        );
    }

    /**
     * 특정 Knowledge 폴더의 증분 노트 목록을 조회한다.
     * <p>
     * 통합 노트는 목록에서 제외하며,
     * 새롭게 생성된 증분 노트만 최신순으로 반환한다.
     */
    public List<KnowledgeNoteSummaryResponse> findNotes(
            Long userId,
            Long knowledgeId
    ) {
        return knowledgeNoteRepository
                .findAllByUserIdAndKnowledgeIdAndTypeOrderByCreatedAtDesc(
                        userId,
                        knowledgeId,
                        KnowledgeNoteType.INCREMENTAL
                )
                .stream()
                .map(
                        KnowledgeNoteSummaryResponse::from
                )
                .toList();
    }

    public List<KnowledgeTreeResponse> buildTree(
            List<Knowledge> knowledgeList
    ) {
        Map<Long, KnowledgeTreeNode> nodeMap =
                new LinkedHashMap<>();

        for (Knowledge knowledge : knowledgeList) {
            nodeMap.put(
                    knowledge.getId(),
                    KnowledgeTreeNode.from(
                            knowledge
                    )
            );
        }

        List<KnowledgeTreeNode> rootNodes =
                new ArrayList<>();

        for (Knowledge knowledge : knowledgeList) {
            KnowledgeTreeNode currentNode =
                    nodeMap.get(
                            knowledge.getId()
                    );

            Knowledge parent =
                    knowledge.getParent();

            if (parent == null) {
                rootNodes.add(
                        currentNode
                );

                continue;
            }

            KnowledgeTreeNode parentNode =
                    nodeMap.get(
                            parent.getId()
                    );

            if (parentNode != null) {
                parentNode.addChild(
                        currentNode
                );
            }
        }

        return rootNodes.stream()
                .map(
                        KnowledgeTreeNode::toResponse
                )
                .toList();
    }

    private static class KnowledgeTreeNode {

        private final Long id;

        private final String name;

        private final List<KnowledgeTreeNode> children =
                new ArrayList<>();

        private KnowledgeTreeNode(
                Long id,
                String name
        ) {
            this.id = id;
            this.name = name;
        }

        private static KnowledgeTreeNode from(
                Knowledge knowledge
        ) {
            return new KnowledgeTreeNode(
                    knowledge.getId(),
                    knowledge.getName()
            );
        }

        private void addChild(
                KnowledgeTreeNode node
        ) {
            children.add(
                    node
            );
        }

        private KnowledgeTreeResponse toResponse() {
            return new KnowledgeTreeResponse(
                    id,
                    name,
                    children.stream()
                            .map(
                                    KnowledgeTreeNode::toResponse
                            )
                            .toList()
            );
        }
    }

    public KnowledgeNoteDetailResponse findConsolidatedNote(
            Long userId,
            Long conversationId
    ) {

        KnowledgeNote note =
                knowledgeNoteRepository
                        .findFirstByUserIdAndConversationIdAndType(
                                userId,
                                conversationId,
                                KnowledgeNoteType.CONSOLIDATED
                        )
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.KNOWLEDGE_NOTE_NOT_FOUND
                                )
                        );

        return KnowledgeNoteDetailResponse.from(
                note
        );
    }
}
