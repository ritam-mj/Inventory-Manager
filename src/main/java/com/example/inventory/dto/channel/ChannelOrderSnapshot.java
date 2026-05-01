package com.example.inventory.dto.channel;

import com.example.inventory.dto.request.OrderItemRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * Order payload from a channel before it is scoped to an internal {@code channelId}.
 */
public record ChannelOrderSnapshot(
        @NotBlank String externalOrderId,
        @NotNull BigDecimal totalAmount,
        @NotEmpty List<OrderItemRequest> items
) {
}
