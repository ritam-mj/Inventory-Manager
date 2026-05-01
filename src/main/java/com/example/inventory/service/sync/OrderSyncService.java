package com.example.inventory.service.sync;

import com.example.inventory.dto.request.OrderRequest;
import com.example.inventory.integration.common.ChannelAdapter;
import com.example.inventory.service.order.OrderProcessingService;
import org.springframework.stereotype.Service;

@Service
public class OrderSyncService {

    private final OrderProcessingService orderProcessingService;

    public OrderSyncService(OrderProcessingService orderProcessingService) {
        this.orderProcessingService = orderProcessingService;
    }

    public void pull(ChannelAdapter adapter, Long channelId) {
        for (var snapshot : adapter.fetchOrders()) {
            OrderRequest normalized = new OrderRequest(
                    snapshot.externalOrderId(),
                    channelId,
                    snapshot.totalAmount(),
                    snapshot.items());
            orderProcessingService.processIncomingOrder(normalized);
        }
    }
}
