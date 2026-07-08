package io.github.kaltz.feldbuch.ai.facade;

import io.github.kaltz.feldbuch.ai.entity.AiSummaryStatus;
import io.github.kaltz.feldbuch.ai.service.AiSummaryAsyncService;
import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.reader.NoteReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiFacade {

    private final AiSummaryAsyncService aiSummaryAsyncService;

    private final NoteReader noteReader;

    public void summarize(
            Long userId,
            Long noteId
    ) {

        Note note = noteReader.get(userId, noteId);

        note.changeSummaryStatus(AiSummaryStatus.PENDING);

        aiSummaryAsyncService.summarize(noteId);
    }
}
