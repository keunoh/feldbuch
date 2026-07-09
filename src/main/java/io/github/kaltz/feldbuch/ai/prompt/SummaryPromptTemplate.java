package io.github.kaltz.feldbuch.ai.prompt;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import org.springframework.stereotype.Component;

@Component
public class SummaryPromptTemplate {

    public String create(SummaryRequest request) {
        return """
                당신은 숙련된 백엔드 개발 멘토입니다.
                아래 개발 노트를 읽고 핵심 내용을 한국어로
                3문장 이내로 요악하세요.
                ===== 노트 =====
                제목
                %s
                내용
                %s
                ===============
                """.formatted(
                request.title(),
                request.content());
    }
}
