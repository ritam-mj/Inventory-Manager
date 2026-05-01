package com.example.inventory.dto.response;

public record InventoryResponse(
        Long skuId,
        int availableQty,
        int reservedQty,
        int safetyStock,
        int sellableQty
) {
}
