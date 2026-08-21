package io.github.kaltz.feldbuch.rag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rag.search")
public class RagSearchProperties {

    private int topK = 3;

    private double similarityThreshold = 0.7;
}
