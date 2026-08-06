package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeCategory;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;

public final class KnowledgeMergePrompt {

    private KnowledgeMergePrompt() {
    }

    /**
     * 기존 통합 노트와 새 증분 노트를 병합하는 AI의 역할,
     * 선택 가능한 카테고리와 출력 형식을 정의한다.
     */
    public static String systemPrompt() {
        return """
                당신은 기존 통합 학습 노트와 새 증분 학습 노트를
                하나의 정돈된 통합 노트로 병합하는 지식 관리 도우미입니다.
                
                기존 통합 노트의 핵심 내용을 유지하면서,
                새 증분 노트에서 추가된 학습 내용을 자연스럽게 반영하세요.
                
                같은 내용은 반복하지 말고 중복을 제거하세요.
                
                category는 서비스에서 미리 정의한 KnowledgeCategory 중
                하나만 선택해야 합니다.
                
                새로운 카테고리 이름을 임의로 만들면 안 됩니다.
                
                반드시 다음 JSON 형식으로만 응답하세요.
                
                {
                  "category": "KnowledgeCategory enum 이름",
                  "title": "통합 학습 노트 제목",
                  "description": "통합 내용을 설명한 한 문장",
                  "summary": "기존 내용과 신규 내용을 병합한 학습 요약",
                  "keywords": [
                    "키워드 1",
                    "키워드 2",
                    "키워드 3"
                  ]
                }
                
                선택 가능한 category 목록:
                
                %s
                
                작성 규칙:
                
                1. JSON 외의 문장을 출력하지 마세요.
                2. Markdown 코드 블록을 사용하지 마세요.
                3. 모든 필드는 반드시 포함하세요.
                4. category는 위 목록에 있는 enum 이름 중 하나만 선택하세요.
                5. category에는 displayName이 아니라 enum 이름을 작성하세요.
                6. 목록에 없는 category를 새로 만들지 마세요.
                7. 기존 통합 노트의 핵심 개념을 삭제하지 마세요.
                8. 새 증분 노트의 내용을 기존 내용과 자연스럽게 통합하세요.
                9. 중복되거나 비슷한 설명은 하나로 정리하세요.
                10. 기존 통합 노트와 새 증분 노트가 같은 기술에 관한 내용이면 기존 category를 유지하세요.
                11. 새 증분 노트가 기존 통합 노트의 세부 개념이라면 category를 변경하지 마세요.
                12. Spring Batch의 Job, Step, Tasklet, Chunk, Retry에 관한 내용은 SPRING_BATCH를 유지하세요.
                13. JPA의 영속성 컨텍스트, Entity, flush, 변경 감지에 관한 내용은 JPA를 유지하세요.
                14. QueryDSL의 Q타입, 동적 쿼리, BooleanExpression에 관한 내용은 QUERYDSL을 유지하세요.
                15. MySQL의 인덱스, 실행 계획, 테이블 구조에 관한 내용은 MYSQL을 유지하세요.
                16. 여러 기술이 함께 등장하더라도 통합 노트의 중심이 되는 하나의 category만 선택하세요.
                17. 기존 category가 명백히 잘못된 경우에만 더 적합한 category로 변경하세요.
                18. title은 전체 통합 내용을 대표하도록 간결하고 구체적으로 작성하세요.
                19. category 이름을 그대로 title로 반복하기보다 실제 학습 범위를 드러내세요.
                20. description은 통합 노트의 성격을 설명하는 짧은 한 문장으로 작성하세요.
                21. summary에는 기존 내용과 신규 내용을 모두 반영하세요.
                22. summary는 항목을 단순 연결하지 말고 하나의 학습 문서처럼 자연스럽게 정리하세요.
                23. keywords는 중복을 제거하고 3개 이상 7개 이하로 작성하세요.
                24. 기존 키워드와 신규 키워드 중 검색에 유용한 항목을 우선 유지하세요.
                25. 결과는 한국어로 작성하되 기술 고유명사는 일반적으로 사용하는 영문 표기를 유지하세요.
                
                응답 예시:
                
                기존 통합 노트가 Spring Batch의 Job과 Step을 다루고,
                새 증분 노트가 Tasklet과 Chunk를 다루는 경우:
                
                {
                  "category": "SPRING_BATCH",
                  "title": "Spring Batch 실행 구조와 처리 방식",
                  "description": "Job과 Step부터 Tasklet과 Chunk까지 Spring Batch의 핵심 실행 구조를 정리한 통합 노트",
                  "summary": "Spring Batch에서 Job은 전체 배치 작업을 나타내고 Step은 실제 처리 단위를 담당합니다. Step은 단일 작업 중심의 Tasklet 방식이나 여러 Item을 일정 단위로 처리하는 Chunk 방식으로 구성할 수 있습니다.",
                  "keywords": [
                    "Spring Batch",
                    "Job",
                    "Step",
                    "Tasklet",
                    "Chunk"
                  ]
                }
                
                기존 통합 노트가 JPA 영속성 컨텍스트를 다루고,
                새 증분 노트가 flush와 변경 감지를 다루는 경우:
                
                {
                  "category": "JPA",
                  "title": "영속성 컨텍스트와 변경 반영 과정",
                  "description": "JPA가 Entity를 관리하고 변경 내용을 데이터베이스에 반영하는 과정을 정리한 통합 노트",
                  "summary": "영속성 컨텍스트는 Entity를 관리하며 1차 캐시와 동일성 보장 기능을 제공합니다. 트랜잭션 커밋이나 flush 시점에는 변경 감지를 통해 수정된 Entity의 SQL이 생성되어 데이터베이스에 반영됩니다.",
                  "keywords": [
                    "JPA",
                    "영속성 컨텍스트",
                    "flush",
                    "변경 감지"
                  ]
                }
                """.formatted(
                KnowledgeCategory.toPromptList()
        );
    }

    /**
     * 기존 통합 노트와 새 증분 노트를
     * AI 사용자 메시지 형식으로 만든다.
     */
    public static String userPrompt(
            KnowledgeNote consolidatedNote,
            KnowledgeNote incrementalNote
    ) {
        validateConsolidatedNote(
                consolidatedNote
        );

        validateIncrementalNote(
                incrementalNote
        );

        return """
                아래 통합 학습 노트와 새 증분 학습 노트를
                하나의 통합 노트로 병합하세요.
                
                category는 반드시 시스템 메시지에 제공된
                KnowledgeCategory enum 중 하나만 선택하세요.
                
                새 증분 노트가 기존 통합 노트의 세부 학습 내용이라면
                기존 기술 category를 유지하세요.
                
                <consolidated-note>
                제목:
                %s
                
                설명:
                %s
                
                요약:
                %s
                
                키워드:
                %s
                </consolidated-note>
                
                <incremental-note>
                제목:
                %s
                
                설명:
                %s
                
                요약:
                %s
                
                키워드:
                %s
                </incremental-note>
                """.formatted(
                consolidatedNote.getTitle(),
                consolidatedNote.getDescription(),
                consolidatedNote.getSummary(),
                String.join(
                        ", ",
                        consolidatedNote.getKeywords()
                ),
                incrementalNote.getTitle(),
                incrementalNote.getDescription(),
                incrementalNote.getSummary(),
                String.join(
                        ", ",
                        incrementalNote.getKeywords()
                )
        );
    }

    private static void validateConsolidatedNote(
            KnowledgeNote note
    ) {
        if (note == null) {
            throw new IllegalArgumentException(
                    "통합 KnowledgeNote는 필수입니다."
            );
        }

        if (!note.isConsolidated()) {
            throw new IllegalArgumentException(
                    "통합 노트 유형만 기존 노트로 사용할 수 있습니다."
            );
        }
    }

    private static void validateIncrementalNote(
            KnowledgeNote note
    ) {
        if (note == null) {
            throw new IllegalArgumentException(
                    "증분 KnowledgeNote는 필수입니다."
            );
        }

        if (!note.isIncremental()) {
            throw new IllegalArgumentException(
                    "증분 노트 유형만 신규 노트로 사용할 수 있습니다."
            );
        }
    }
}