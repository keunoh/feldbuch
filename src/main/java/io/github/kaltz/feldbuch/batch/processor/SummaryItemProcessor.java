package io.github.kaltz.feldbuch.batch.processor;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import io.github.kaltz.feldbuch.ai.service.SummaryService;
import io.github.kaltz.feldbuch.note.entity.Note;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SummaryItemProcessor implements ItemProcessor<Note, Note> {

    private final SummaryService summaryService;

    @Override
    public Note process(Note note) throws Exception {

        log.info("===== Processor =====");
        log.info("Note Id : {}", note.getId());

        SummaryRequest request = SummaryRequest.from(note);
        String summary = summaryService.summarize(request);
        note.completeSummary(summary);

        log.info("Summary Completed : {}", note.getId());

        return note;
    }
}
