package com.example.inventory.lock;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class InMemoryDistributedLockService implements DistributedLockService {

    private final Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public <T> T executeWithLock(String key, Callable<T> callable) {
        ReentrantLock lock = lockMap.computeIfAbsent(key, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException("Lock execution failed", e);
        } finally {
            lock.unlock();
        }
    }
}
