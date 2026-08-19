package io.github.haidarim.shard.integrationtest.common;



import io.github.haidarim.shard.ShardingApplication;
import io.github.haidarim.shard.base.repository.ShardMapRepository;
import io.github.haidarim.shard.base.repository.VirtualShardMapRepository;
import io.github.haidarim.shard.generated.grpc.ShardMapApiServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AbstractShardTest {
    @Autowired
    private ShardMapRepository repository;

    @Autowired
    private VirtualShardMapRepository virtualShardMapRepository;

    private final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379);

    private  ManagedChannel channel;

    // For asynchronous call, calls StreamObserver when the response arrives
    // private ShardMapApiServiceGrpc.ShardMapApiServiceStub stub;

    // asynchronous with ListenableFuture, can attach a callback:
    // private ShardMapApiServiceGrpc.ShardMapApiServiceFutureStub stub;

    // For synchronous call
    private  ShardMapApiServiceGrpc.ShardMapApiServiceBlockingStub stub;

    @BeforeEach
    public void before(){
        virtualShardMapRepository.deleteAll();
        repository.deleteAll();

        postgresContainer.start();
        redisContainer.start();

        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());

        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(redisContainer.getMappedPort(6379)));

        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        stub = ShardMapApiServiceGrpc.newBlockingStub(channel);

        preTest();
    }

    @AfterEach
    public void tearDown(){
        channel.shutdown();
        postgresContainer.stop();
        redisContainer.stop();
        postTest();
    }

    public void preTest(){}

    public void postTest(){}
}
