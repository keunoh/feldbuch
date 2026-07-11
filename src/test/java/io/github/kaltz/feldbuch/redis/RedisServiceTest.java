package io.github.kaltz.feldbuch.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisServiceTest {

    @Autowired
    private RedisService redisService;

    @Test
    @DisplayName("Redis 저장 및 조회")
    void saveAndGet() {
        redisService.save("note:1", "Spring Boot");

        assertThat(redisService.get("note:1")).isEqualTo("Spring Boot");
    }
}