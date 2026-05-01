package com.example.inventory.lock;

import java.util.concurrent.Callable;

public interface DistributedLockService {
    <T> T executeWithLock(String key, Callable<T> callable);
}
