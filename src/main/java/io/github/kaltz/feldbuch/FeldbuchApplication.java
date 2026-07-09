package io.github.kaltz.feldbuch;

import io.github.kaltz.feldbuch.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(JwtProperties.class)
@EnableAsync
public class FeldbuchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeldbuchApplication.class, args);
    }

}
