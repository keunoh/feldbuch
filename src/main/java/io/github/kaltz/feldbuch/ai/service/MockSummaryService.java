package io.github.kaltz.feldbuch.ai.service;

import org.springframework.stereotype.Service;

@Service
public class MockSummaryService implements SummaryService {

    @Override
    public String summarize(String content) {

        if (content.length() <= 100) {
            return content;
        }

        return content.substring(0, 100) + "...";
    }
}
