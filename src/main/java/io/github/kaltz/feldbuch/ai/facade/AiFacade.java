package io.github.kaltz.feldbuch.ai.facade;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import io.github.kaltz.feldbuch.ai.mapper.SummaryMapper;
import io.github.kaltz.feldbuch.ai.service.SummaryService;
import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.reader.NoteReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiFacade {

    private final SummaryService summaryService;
    private final NoteReader noteReader;

    public void summarize(
            Long userId,
            Long noteId
    ) {
        Note note = noteReader.get(
                userId, noteId);

        SummaryRequest request = SummaryMapper.toRequest(note);

        String summary = summaryService.summarize(request);

        note.changeSummary(summary);
    }
}
