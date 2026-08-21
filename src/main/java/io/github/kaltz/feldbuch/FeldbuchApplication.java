package io.github.kaltz.feldbuch;

import io.github.kaltz.feldbuch.config.JwtProperties;
import io.github.kaltz.feldbuch.rag.config.RagSearchProperties;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(
        exclude = PgVectorStoreAutoConfiguration.class
)
@EnableJpaAuditing
@EnableConfigurationProperties({
        JwtProperties.class,
        RagSearchProperties.class,
})
@EnableAsync
@EnableCaching
public class FeldbuchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeldbuchApplication.class, args);
    }

}
