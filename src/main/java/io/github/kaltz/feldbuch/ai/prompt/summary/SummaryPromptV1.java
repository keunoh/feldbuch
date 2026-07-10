package io.github.kaltz.feldbuch.ai.prompt.summary;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import org.springframework.stereotype.Component;

@Component
public class SummaryPromptV1 implements SummaryPrompt {

    @Override
    public String create(SummaryRequest request) {
        return """
                당신은 10년차 Spring Backend 개발자입니다.
                다음 개발 노트를 3문장 이하로 요약하세요.
                [제목]
                %s
                
                [카테고리]
                %s
                
                [학습상태]
                %s
                
                [내용]
                %s
                """
                .formatted(
                        request.title(),
                        request.category(),
                        request.studyStatus(),
                        request.content()
                );
    }
}
