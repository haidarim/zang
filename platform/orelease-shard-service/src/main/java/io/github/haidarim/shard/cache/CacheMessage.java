package io.github.haidarim.shard.cache;



public record CacheMessage(
        Cache.CacheEntity entity,
        Cache.CacheEventType eventType,
        Object id
) {

    public static CacheMessage created(
        Cache.CacheEntity entity,
        Object id
    ){
        return new CacheMessage(
                entity,
                Cache.CacheEventType.CREATED,
                id
        );
    }

    public static CacheMessage updated(
            Cache.CacheEntity entity,
            Object id
    ){
        return new CacheMessage(
                entity,
                Cache.CacheEventType.UPDATED,
                id
        );
    }

    public static CacheMessage deleted(
            Cache.CacheEntity entity,
            Object id
    ){
        return new CacheMessage(
                entity,
                Cache.CacheEventType.DELETED,
                id
        );
    }

    public static CacheMessage refresh(
            Cache.CacheEntity entity,
            Object id
    ){
        return new CacheMessage(
                entity,
                Cache.CacheEventType.REFRESH,
                id
        );
    }
}
