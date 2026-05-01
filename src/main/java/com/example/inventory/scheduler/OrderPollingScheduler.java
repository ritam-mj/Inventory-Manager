package com.example.inventory.scheduler;

import com.example.inventory.service.sync.SyncOrchestrator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderPollingScheduler {

    private final SyncOrchestrator syncOrchestrator;

    public OrderPollingScheduler(SyncOrchestrator syncOrchestrator) {
        this.syncOrchestrator = syncOrchestrator;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.order-poll-delay-ms:300000}")
    public void pollOrders() {
        syncOrchestrator.pollOrdersFromChannels();
    }
}
