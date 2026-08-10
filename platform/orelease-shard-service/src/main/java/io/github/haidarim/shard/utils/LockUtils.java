package io.github.haidarim.shard.utils;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public final class LockUtils {

    public static <T> void removeLock(Map<T, ReentrantLock> locks, T id, ReentrantLock lock) {
        if (!lock.hasQueuedThreads() && !lock.isLocked()) {
            locks.remove(id, lock);
        }
    }
}
