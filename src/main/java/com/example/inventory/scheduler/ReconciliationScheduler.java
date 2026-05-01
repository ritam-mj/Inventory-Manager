package com.example.inventory.scheduler;

import com.example.inventory.service.sync.ReconciliationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationScheduler {

    private final ReconciliationService reconciliationService;

    public ReconciliationScheduler(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void reconcile() {
        reconciliationService.compareDbVsChannels();
    }
}
