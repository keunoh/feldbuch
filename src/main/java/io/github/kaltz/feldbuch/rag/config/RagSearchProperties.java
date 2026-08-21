package io.github.kaltz.feldbuch.rag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rag.search")
public class RagSearchProperties {

    /**
     * RagSearchProperties에도 기본값 0.7을 넣어두었다면 같이 0.3으로 변경해야 해.
     * 설정 파일이 없는 환경에서도 동일한 기본 정책이 적용되어야 하니까.
     */
    private int topK = 3;

    private double similarityThreshold = 0.3;
}
