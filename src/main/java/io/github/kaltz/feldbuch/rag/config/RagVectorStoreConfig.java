package io.github.kaltz.feldbuch.rag.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class RagVectorStoreConfig {

    @Bean(name = "ragDataSource")
    public DataSource ragDataSource(
            @Value("${rag.datasource.url}") String url,
            @Value("${rag.datasource.username}") String username,
            @Value("${rag.datasource.password}") String password
    ) {
        HikariDataSource dataSource =
                new HikariDataSource();

        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(
                "org.postgresql.Driver"
        );

        return dataSource;
    }

    @Bean
    public JdbcTemplate ragJdbcTemplate(
            @Qualifier("ragDataSource")
            DataSource ragDataSource
    ) {
        return new JdbcTemplate(ragDataSource);
    }

    @Bean
    public VectorStore vectorStore(
            @Qualifier("ragJdbcTemplate")
            JdbcTemplate ragJdbcTemplate,
            EmbeddingModel embeddingModel
    ) {
        return PgVectorStore.builder(
                        ragJdbcTemplate,
                        embeddingModel
                )
                .initializeSchema(true)
                .build();
    }
}
