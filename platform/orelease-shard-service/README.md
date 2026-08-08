## Shard Service 

Control plane:

Core APIs:
1. ResolveShard
2. GetShardTopology
3. AcquireLock
4. ReleaseLock
5. StartMigration
6. GetMigrationStatus


### Between microservices and shard service: 
gRPC is used. ![proto-file-guide](https://www.google.com/url?sa=t&source=web&rct=j&opi=89978449&url=https://protobuf.dev/programming-guides/proto3/&ved=2ahUKEwj6-6m52oGWAxWmKhAIHTSqEa8QFnoECBgQAQ&usg=AOvVaw243OflOI0-hDPRf8pDHIGq)


### Request flow:

```txt

                 +----------------+
                 | shard-service  |
                 | PostgreSQL     |
                 +----------------+
                         ^
                         |
                      gRPC
                         |
                         v

                 +----------------+
                 | user-service   |
                 | chat-service   |
                 +----------------+
                         |
                         |
                 Redis cache
                         |
                         |
              Dynamic DataSource Router
                         |
                         |
             +-----------+-----------+
             |           |           |
             v           v           v

          shard-1     shard-2     shard-3
          postgres    postgres    postgres
```


```txt
ShardMapService
    |
    +-- create physical shard
    +-- update shard metadata
    +-- deactivate shard


ShardNodeService
    |
    +-- add node to existing shard
    +-- update node
    +-- remove node


VirtualShardMapService
    |
    +-- create virtual->physical mappings
    +-- update mappings
    
   
```


```txt
    Keep instances of cashe consistence 
    Caffeine instead of map for localCache
    Distributed locks with redis instead of local locks
```