package com.example.inventory.dto.response;

import com.example.inventory.domain.order.OrderStatus;
import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        String externalOrderId,
        Long channelId,
        OrderStatus status,
        BigDecimal totalAmount
) {
}
