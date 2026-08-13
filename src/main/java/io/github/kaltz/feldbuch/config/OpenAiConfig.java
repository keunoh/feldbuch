package io.github.kaltz.feldbuch.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(120);

    private static final Duration CONNECTION_MAX_IDLE_TIME =
            Duration.ofSeconds(30);

    private static final Duration CONNECTION_MAX_LIFE_TIME =
            Duration.ofMinutes(5);

    private static final Duration EVICT_INTERVAL =
            Duration.ofSeconds(30);

    private final OpenAiProperties properties;

    // 이제 프로젝트 어디에서든
    // private final RestClient openAiRestClient;
    // 주입 가능하다.
    @Bean
    public RestClient openAiRestClient() {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.getApiKey()
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public WebClient openAiWebClient() {
        
        ConnectionProvider.builder("openai")
                .maxIdleTime(
                        CONNECTION_MAX_IDLE_TIME
                )
                .maxLifeTime(
                        CONNECTION_MAX_LIFE_TIME
                )
                .pendingAcquireTimeout(
                        CONNECT_TIMEOUT
                )
                .evictInBackground(
                        EVICT_INTERVAL
                )
                .build();

        HttpClient httpClient =
                HttpClient.create()
                        .option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                Math.toIntExact(
                                        CONNECT_TIMEOUT.toMillis()
                                )
                        )
                        .responseTimeout(
                                READ_TIMEOUT
                        );

        ReactorClientHttpConnector connector =
                new ReactorClientHttpConnector(httpClient);


        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.getApiKey()
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .clientConnector(connector)
                .build();
    }
}
