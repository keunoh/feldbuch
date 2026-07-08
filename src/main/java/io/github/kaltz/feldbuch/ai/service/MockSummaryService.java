package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import org.springframework.stereotype.Service;

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
