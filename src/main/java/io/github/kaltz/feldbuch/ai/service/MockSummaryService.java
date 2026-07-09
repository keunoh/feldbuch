package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import org.springframework.stereotype.Service;

// 테스트 때문이다.
// 통합 테스트에서는 MockSummaryService가 필요하다.
// OpenAI를 매번 호출하면
// 느리다, 돈이 든다, 네트워크 의존성이 생긴다.
@Service
public class MockSummaryService implements SummaryService {

    @Override
    public String summarize(SummaryRequest request) {

        String content = request.content();

        if (content == null || content.isBlank()) {
            return "";
        }

        if (content.length() <= 100) {
            return content;
        }

        return content.substring(0, 100) + "...";
    }
}
