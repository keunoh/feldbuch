package io.github.kaltz.feldbuch.ai.mapper;

import io.github.kaltz.feldbuch.ai.dto.SummaryRequest;
import io.github.kaltz.feldbuch.note.entity.Note;

public final class SummaryMapper {

    private SummaryMapper() {
    }

    public static SummaryRequest toRequest(
            Note note
    ) {
        return new SummaryRequest(
                note.getTitle(),
                note.getContent(),
                note.getCategory().name(),
                note.getStudyStatus().name()
        );
    }
}
