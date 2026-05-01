package com.example.inventory.integration.common;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class ChannelRestExecutor {

    private final RetryRegistry retryRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    public ChannelRestExecutor(RetryRegistry retryRegistry, RateLimiterRegistry rateLimiterRegistry) {
        this.retryRegistry = retryRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    public void run(String channelKey, Runnable action) {
        execute(channelKey, () -> {
            action.run();
            return null;
        });
    }

    public <T> T execute(String channelKey, Supplier<T> supplier) {
        Retry retry = retryRegistry.retry(channelKey);
        RateLimiter limiter = rateLimiterRegistry.rateLimiter(channelKey);
        Supplier<T> decorated = RateLimiter.decorateSupplier(limiter, Retry.decorateSupplier(retry, supplier));
        return decorated.get();
    }
}
