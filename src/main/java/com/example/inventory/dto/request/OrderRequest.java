package com.example.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
        @NotBlank String externalOrderId,
        @NotNull Long channelId,
        @NotNull BigDecimal totalAmount,
        @NotEmpty List<OrderItemRequest> items
) {
}
