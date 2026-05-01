package com.example.inventory.scheduler;

import com.example.inventory.domain.sync.SyncStatus;
import com.example.inventory.repository.SyncLogRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryScheduler {

    private final SyncLogRepository syncLogRepository;

    public RetryScheduler(SyncLogRepository syncLogRepository) {
        this.syncLogRepository = syncLogRepository;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.retry-delay-ms:600000}")
    @Transactional
    public void retryFailed() {
        syncLogRepository.findTop100ByStatusOrderByCreatedAtAsc(SyncStatus.FAILED)
                .forEach(log -> log.setRetryCount(log.getRetryCount() + 1));
    }
}
