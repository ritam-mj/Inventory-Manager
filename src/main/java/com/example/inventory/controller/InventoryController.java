package com.example.inventory.controller;

import com.example.inventory.domain.inventory.Inventory;
import com.example.inventory.dto.request.ReserveStockRequest;
import com.example.inventory.dto.response.InventoryResponse;
import com.example.inventory.service.inventory.InventoryService;
import com.example.inventory.service.inventory.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ReservationService reservationService;

    public InventoryController(InventoryService inventoryService, ReservationService reservationService) {
        this.inventoryService = inventoryService;
        this.reservationService = reservationService;
    }

    @GetMapping("/sku/{skuId}")
    public InventoryResponse bySku(@PathVariable Long skuId) {
        Inventory inv = inventoryService.getBySkuId(skuId);
        return new InventoryResponse(
                skuId,
                inv.getAvailableQty(),
                inv.getReservedQty(),
                inv.getSafetyStock(),
                inventoryService.getSellableQty(skuId));
    }

    @PostMapping("/reserve")
    public void reserve(@RequestBody @Valid ReserveStockRequest request) {
        reservationService.reserveStock(request.skuId(), request.quantity(), null);
    }
}
