package com.example.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReserveStockRequest(
        @NotNull Long skuId,
        @Min(1) int quantity
) {
}
