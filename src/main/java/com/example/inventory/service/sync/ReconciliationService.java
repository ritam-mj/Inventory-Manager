package com.example.inventory.service.sync;

import com.example.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class ReconciliationService {

    private final InventoryRepository inventoryRepository;

    public ReconciliationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void compareDbVsChannels() {
        inventoryRepository.findAll().forEach(inv -> {
            // Placeholder for channel-side quantity comparison.
        });
    }
}
