package io.github.kaltz.feldbuch.ai.service;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;

public interface SummaryService {

    String summarize(SummaryRequest note);
}
