package com.example.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderItemRequest(
        @NotNull Long skuId,
        @Min(1) int quantity,
        @NotNull BigDecimal price
) {
}
