package io.github.kaltz.feldbuch.conversation.entity;

public enum KnowledgeExtractStatus {
    /**
     * 아직 지식 추출을 수행하지 않은 상태
     */
    NONE,

    /**
     * 지식 추출을 진행 중인 상태
     */
    PROCESSING,

    /**
     * 지식 추출을 완료한 상태
     */
    COMPLETED,

    /**
     * 지식 추출에 실패한 상태
     */
    FAILED
}
