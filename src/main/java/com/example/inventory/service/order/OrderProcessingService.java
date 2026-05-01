package com.example.inventory.service.order;

import com.example.inventory.domain.order.Order;
import com.example.inventory.domain.order.OrderItem;
import com.example.inventory.dto.request.OrderItemRequest;
import com.example.inventory.dto.request.OrderRequest;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.SkuRepository;
import com.example.inventory.service.inventory.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderProcessingService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final SkuRepository skuRepository;
    private final ReservationService reservationService;

    public OrderProcessingService(
            OrderService orderService,
            OrderRepository orderRepository,
            SkuRepository skuRepository,
            ReservationService reservationService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.skuRepository = skuRepository;
        this.reservationService = reservationService;
    }

    @Transactional
    public Order processIncomingOrder(OrderRequest request) {
        if (orderService.exists(request)) {
            return orderRepository.findByExternalOrderIdAndChannelId(request.externalOrderId(), request.channelId()).orElseThrow();
        }

        Order order = orderService.createOrderShell(request);
        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = new OrderItem();
            item.setSku(skuRepository.findById(itemRequest.skuId()).orElseThrow());
            item.setQuantity(itemRequest.quantity());
            item.setPrice(itemRequest.price());
            order.addItem(item);
        }
        Order saved = orderRepository.save(order);
        saved.getItems().forEach(item -> reservationService.reserveStock(item.getSku().getId(), item.getQuantity(), item));
        return saved;
    }
}
