package com.example.inventory.scheduler;

import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.service.sync.InventorySyncService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventorySyncScheduler {

    private final InventoryRepository inventoryRepository;
    private final InventorySyncService inventorySyncService;

    public InventorySyncScheduler(InventoryRepository inventoryRepository, InventorySyncService inventorySyncService) {
        this.inventoryRepository = inventoryRepository;
        this.inventorySyncService = inventorySyncService;
    }

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional(readOnly = true)
    public void periodicPushSync() {
        inventoryRepository.findAll().forEach(inv -> inventorySyncService.syncInventory(inv.getSku().getId()));
    }
}
