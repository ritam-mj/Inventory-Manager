package com.example.inventory.controller;

import com.example.inventory.service.sync.ReconciliationService;
import com.example.inventory.service.sync.SyncOrchestrator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SyncOrchestrator syncOrchestrator;
    private final ReconciliationService reconciliationService;

    public AdminController(SyncOrchestrator syncOrchestrator, ReconciliationService reconciliationService) {
        this.syncOrchestrator = syncOrchestrator;
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/poll-orders")
    public void pollOrders() {
        syncOrchestrator.pollOrdersFromChannels();
    }

    @PostMapping("/reconcile")
    public void reconcile() {
        reconciliationService.compareDbVsChannels();
    }
}
