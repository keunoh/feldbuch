package io.github.kaltz.feldbuch.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RagVectorStoreTest {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private Environment environment;

    @Test
    void openAi설정확인() {
        String apiKey =
                environment.getProperty("spring.ai.openai.api-key");

        String model =
                environment.getProperty(
                        "spring.ai.openai.embedding.options.model"
                );

        System.out.println(
                "model = " + model
        );

        System.out.println(
                "apiKey prefix = "
                        + apiKey.substring(
                        0,
                        Math.min(12, apiKey.length())
                )
        );
    }

    @Test
    void 벡터_저장_후_유사한_문장을_검색한다() {

        // given
        Document document =
                new Document(
                        "Spring에서 트랜잭션은 @Transactional을 사용한다.",
                        Map.of(
                                "type", "knowledge",
                                "topic", "spring-transaction"
                        )
                );

        vectorStore.add(
                List.of(document)
        );

        // when
        List<Document> results =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(
                                        "스프링 트랜잭션은 어떻게 사용하지?"
                                )
                                .topK(3)
                                .similarityThresholdAll()
                                .build()
                );

        // then
        assertThat(results)
                .isNotEmpty();

        results.forEach(result ->
                System.out.println(
                        "검색 결과: "
                                + result.getText()
                                + " / metadata: "
                                + result.getMetadata()
                )
        );
    }
}