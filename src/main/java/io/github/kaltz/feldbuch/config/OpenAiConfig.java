package io.github.kaltz.feldbuch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    private final OpenAiProperties properties;

    // 이제 프로젝트 어디에서든
    // private final RestClient openAiRestClient;
    // 주입 가능하다.
    @Bean
    public RestClient openAiRestClient() {

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(
                        "Authorization",
                        "Bearer " + properties.getKey()
                )
                .defaultHeader(
                        "Content-Type",
                        "application/json"
                )
                .build();
    }
}
