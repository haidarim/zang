package io.github.haidarim.shard.integrationtest.common;



import io.github.haidarim.shard.ShardingApplication;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(
    classes = {
            ShardingApplication.class
    }
)
@ActiveProfiles("test")
public class AbstractIntegrationTest {

    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379);

    static {
            postgresContainer.start();
            redisContainer.start();

            System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
            System.setProperty("spring.datasource.username", postgresContainer.getUsername());
            System.setProperty("spring.datasource.password", postgresContainer.getPassword());

            System.setProperty("spring.data.redis.host", redisContainer.getHost());
            System.setProperty("spring.data.redis.port", String.valueOf(redisContainer.getMappedPort(6379)));
    }
}
