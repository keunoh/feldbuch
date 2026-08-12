package io.github.kaltz.feldbuch.batch.controller;

import io.github.kaltz.feldbuch.batch.scheduler.KnowledgeExtractionScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/batch")
public class BatchAdminController {

    private static final String BATCH_LOG =
            "[KNOWLEDGE_EXTRACTION_MANUAL]";

    private final KnowledgeExtractionScheduler
            knowledgeExtractionScheduler;

    /**
     * 지식 추출 배치를 수동으로 실행한다.
     * <p>
     * 운영 환경에서 스케줄 실행 시각을 기다리지 않고
     * 지식 추출 배치를 즉시 실행하기 위한 임시 API.
     */
    @PostMapping("/knowledge-extraction")
    public ResponseEntity<Void> runKnowledgeExtraction() {

        log.info(
                "{} Manual execution requested.",
                BATCH_LOG
        );

        knowledgeExtractionScheduler.run();

        log.info(
                "{} Manual execution completed.",
                BATCH_LOG
        );

        return ResponseEntity.ok().build();
    }
}