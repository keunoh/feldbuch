package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeNote;
import io.github.kaltz.feldbuch.knowledge.entity.KnowledgeRootCategory;

public final class KnowledgeMergePrompt {

    private KnowledgeMergePrompt() {
    }

    public static String systemPrompt() {
        return """
                당신은 기존 학습 노트와 새 대화 내용을 하나의 정돈된 학습 노트로 병합하는 지식 관리 도우미입니다.
                
                기존 노트의 핵심 내용을 유지하면서 새 대화에서 추가된 학습 내용을 자연스럽게 반영하세요.
                같은 내용을 반복해서 작성하지 말고 중복을 제거하세요.
                
                대분류는 반드시 아래 목록 중 하나를 선택해야 합니다.
                
                %s
                
                반드시 다음 JSON 형식으로만 응답하세요.
                
                {
                  "rootCategory": "WEB_DEVELOPMENT",
                  "knowledgePath": ["Spring Framework", "WebFlux"],
                  "title": "학습 노트 제목",
                  "description": "학습 내용을 설명한 한 문장",
                  "summary": "기존 내용과 신규 내용을 병합한 학습 요약",
                  "keywords": ["키워드 1", "키워드 2", "키워드 3"]
                }
                
                작성 규칙:
                
                1. JSON 외의 문장을 출력하지 마세요.
                2. Markdown 코드 블록을 사용하지 마세요.
                3. 기존 노트의 핵심 개념을 삭제하지 마세요.
                4. 새 대화에서 추가된 개념을 기존 내용과 자연스럽게 통합하세요.
                5. 반복되는 문장은 제거하세요.
                6. rootCategory에는 enum 코드만 작성하세요.
                7. knowledgePath에는 대분류를 포함하지 마세요.
                8. knowledgePath는 최대 2단계까지만 작성하세요.
                9. title은 전체 내용을 대표하도록 필요하면 수정하세요.
                10. description과 summary는 기존 내용과 신규 내용을 모두 반영하세요.
                11. keywords는 중복을 제거하고 3개 이상 7개 이하로 작성하세요.
                """.formatted(
                KnowledgeRootCategory.toPromptList()
        );
    }

    public static String userPrompt(
            KnowledgeNote existingNote,
            String newConversationContext
    ) {
        if (existingNote == null) {
            throw new IllegalArgumentException(
                    "기존 KnowledgeNote는 필수입니다."
            );
        }

        if (
                newConversationContext == null
                        || newConversationContext.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "새 대화 내용은 필수입니다."
            );
        }

        return """
                아래 기존 학습 노트와 새 대화 내용을 하나의 학습 노트로 병합하세요.
                
                <existing-note>
                제목:
                %s
                
                설명:
                %s
                
                요약:
                %s
                
                키워드:
                %s
                </existing-note>
                
                <new-conversation>
                %s
                </new-conversation>
                """.formatted(
                existingNote.getTitle(),
                existingNote.getDescription(),
                existingNote.getSummary(),
                String.join(
                        ", ",
                        existingNote.getKeywords()
                ),
                newConversationContext.trim()
        );
    }
}
