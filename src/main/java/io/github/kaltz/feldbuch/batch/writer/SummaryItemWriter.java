package io.github.kaltz.feldbuch.batch.writer;

import io.github.kaltz.feldbuch.note.entity.Note;
import io.github.kaltz.feldbuch.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SummaryItemWriter implements ItemWriter<Note> {

    private final NoteRepository noteRepository;

    @Override
    public void write(Chunk<? extends Note> chunk) throws Exception {
        log.info("===== Writer =====");

        noteRepository.saveAll(chunk.getItems());

        log.info("Save Count : {}", chunk.size());
    }
}
