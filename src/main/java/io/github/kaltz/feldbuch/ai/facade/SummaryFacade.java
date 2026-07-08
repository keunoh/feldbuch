package io.github.kaltz.feldbuch.ai.facade;

import io.github.kaltz.feldbuch.ai.service.SummaryService;
import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.reader.NoteReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummaryFacade {

    private final NoteReader noteReader;

    private final SummaryService summaryService;

    public void summarize(
            Long userId,
            Long noteId
    ) {
        Note note = noteReader.get(userId, noteId);

        String summary = summaryService.summarize(
                note.getContent()
        );

        note.changeSummary(summary);
    }
}
