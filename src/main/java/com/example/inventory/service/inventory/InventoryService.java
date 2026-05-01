package com.example.inventory.service.inventory;

import com.example.inventory.domain.inventory.Inventory;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public Inventory getBySkuId(Long skuId) {
        return inventoryRepository.findBySkuId(skuId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for sku " + skuId));
    }

    @Transactional(readOnly = true)
    public int getSellableQty(Long skuId) {
        Inventory inv = getBySkuId(skuId);
        return Math.max(0, inv.getAvailableQty() - inv.getReservedQty() - inv.getSafetyStock());
    }
}
