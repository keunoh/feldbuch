package io.github.kaltz.feldbuch.note.dto.request;

import io.github.kaltz.feldbuch.note.entity.StudyStatus;

public record UpdateStudyStatusRequest(
        StudyStatus studyStatus
) {
}
