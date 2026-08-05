package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.knowledge.folder.KnowledgeFolderCandidate;

import java.util.List;
import java.util.stream.Collectors;

public final class KnowledgeFolderSelectionPrompt {

    private KnowledgeFolderSelectionPrompt() {
    }

    public static String systemPrompt() {
        return """
                당신은 지식 관리 서비스의 폴더 분류 도우미입니다.
                
                새 지식 폴더를 무분별하게 생성하지 않고,
                의미상 충분히 유사한 기존 폴더가 있다면 기존 폴더를 선택해야 합니다.
                
                반드시 아래 JSON 형식으로만 응답하세요.
                
                기존 폴더를 재사용하는 경우:
                {
                  "selectionType": "EXISTING",
                  "selectedKnowledgeId": 1
                }
                
                새 폴더가 반드시 필요한 경우:
                {
                  "selectionType": "CREATE",
                  "selectedKnowledgeId": null
                }
                
                판단 규칙:
                
                1. JSON 외의 문장을 출력하지 마세요.
                2. Markdown 코드 블록을 사용하지 마세요.
                3. 기존 후보 중 의미가 충분히 가까운 폴더가 있으면 EXISTING을 선택하세요.
                4. 단순한 표현 차이, 약어 차이, 한글과 영문 차이만으로 새 폴더를 만들지 마세요.
                5. 상위 개념이 기존 폴더에 포함될 수 있다면 기존 폴더를 우선 선택하세요.
                6. 주제와 의미가 실질적으로 다를 때만 CREATE를 선택하세요.
                7. EXISTING을 선택하면 제공된 후보 ID 중 하나만 반환하세요.
                8. CREATE를 선택하면 selectedKnowledgeId는 null이어야 합니다.
                9. 후보 목록에 없는 ID를 임의로 만들지 마세요.
                """;
    }

    public static String userPrompt(
            String rootCategoryName,
            String parentFolderName,
            String requestedFolderName,
            List<KnowledgeFolderCandidate> candidates
    ) {
        validateText(
                rootCategoryName,
                "대분류 이름"
        );

        validateText(
                requestedFolderName,
                "분류할 폴더 이름"
        );

        String candidateText =
                formatCandidates(candidates);

        String normalizedParentName =
                parentFolderName == null
                        ? rootCategoryName
                        : parentFolderName.trim();

        return """
                다음 새 폴더를 현재 위치의 기존 폴더 중 하나로 분류할 수 있는지 판단하세요.
                
                대분류:
                %s
                
                현재 상위 폴더:
                %s
                
                분류할 폴더 이름:
                %s
                
                현재 위치의 기존 하위 폴더:
                %s
                
                의미가 충분히 비슷한 기존 폴더가 있으면 EXISTING을 선택하세요.
                기존 폴더로 분류하기 어렵다면 CREATE를 선택하세요.
                """.formatted(
                rootCategoryName.trim(),
                normalizedParentName,
                requestedFolderName.trim(),
                candidateText
        );
    }

    private static String formatCandidates(
            List<KnowledgeFolderCandidate> candidates
    ) {
        if (
                candidates == null
                        || candidates.isEmpty()
        ) {
            return "(없음)";
        }

        return candidates.stream()
                .map(candidate ->
                        "- id=%d, name=%s".formatted(
                                candidate.id(),
                                candidate.name()
                        )
                )
                .collect(
                        Collectors.joining(
                                System.lineSeparator()
                        )
                );
    }

    private static void validateText(
            String value,
            String fieldName
    ) {
        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    fieldName + "은 필수입니다."
            );
        }
    }
}
