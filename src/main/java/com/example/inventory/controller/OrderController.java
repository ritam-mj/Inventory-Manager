package com.example.inventory.controller;

import com.example.inventory.domain.order.Order;
import com.example.inventory.dto.request.OrderRequest;
import com.example.inventory.dto.response.OrderResponse;
import com.example.inventory.service.order.OrderProcessingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProcessingService orderProcessingService;

    public OrderController(OrderProcessingService orderProcessingService) {
        this.orderProcessingService = orderProcessingService;
    }

    @PostMapping
    public OrderResponse create(@RequestBody @Valid OrderRequest request) {
        Order order = orderProcessingService.processIncomingOrder(request);
        return new OrderResponse(
                order.getId(),
                order.getExternalOrderId(),
                order.getChannel().getId(),
                order.getStatus(),
                order.getTotalAmount());
    }
}
