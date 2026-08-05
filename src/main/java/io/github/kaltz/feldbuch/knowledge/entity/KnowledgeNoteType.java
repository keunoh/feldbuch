package io.github.kaltz.feldbuch.knowledge.entity;

public enum KnowledgeNoteType {
    /**
     * 매번 새로 생성되는 학습 노트
     */
    INCREMENTAL,

    /**
     * 같은 Conversation의 누적 대표 노트
     */
    CONSOLIDATED
}
