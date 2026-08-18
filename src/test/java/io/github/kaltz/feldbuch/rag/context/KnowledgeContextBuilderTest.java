package io.github.kaltz.feldbuch.rag.context;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeContextBuilderTest {

    private final KnowledgeContextBuilder builder
            = new KnowledgeContextBuilder();

    @Test
    void 검색된_지식을_Context로_변환한다() {

        // given
        Document first =
                new Document(
                        """
                                제목: Spring 트랜잭션
                                내용:
                                @Transactional을 사용합니다.
                                """
                );

        Document second =
                new Document(
                        """
                                제목: JPA 변경 감지
                                내용:
                                flush 시점에 변경 사항을 반영합니다.
                                """
                );

        // when
        String context = builder.build(List.of(first, second));

        // then
        assertThat(context)
                .contains("[지식 1]")
                .contains("Spring 트랜잭션")
                .contains("@Transactional")
                .contains("[지식 2]")
                .contains("flush");
    }

    @Test
    void 검색된_지식이_없으면_빈_문자열을_반환한다() {

        // when
        String context = builder.build(List.of());

        // then
        assertThat(context).isEmpty();
    }

}