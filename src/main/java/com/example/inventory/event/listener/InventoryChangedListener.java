package com.example.inventory.event.listener;

import com.example.inventory.event.InventoryChangedEvent;
import com.example.inventory.service.sync.InventorySyncService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class InventoryChangedListener {

    private final InventorySyncService inventorySyncService;

    public InventoryChangedListener(InventorySyncService inventorySyncService) {
        this.inventorySyncService = inventorySyncService;
    }

    @Async("inventoryExecutor")
    @EventListener
    public void onInventoryChanged(InventoryChangedEvent event) {
        inventorySyncService.syncInventory(event.skuId());
    }
}
