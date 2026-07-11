package io.github.kaltz.feldbuch.batch.reader;

import io.github.kaltz.feldbuch.note.entity.Note;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SummaryItemReader {

    private final EntityManagerFactory entityManagerFactory;

    public JpaPagingItemReader<Note> reader() {
        JpaPagingItemReader<Note> reader = new JpaPagingItemReader<>();

        reader.setName("summaryItemReader");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setPageSize(10);
        reader.setQueryString("""
                select n from Note n
                where n.summary is null
                order by n.id
                """);

        return reader;
    }
}
