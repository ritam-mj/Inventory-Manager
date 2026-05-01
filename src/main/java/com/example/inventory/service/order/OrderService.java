package com.example.inventory.service.order;

import com.example.inventory.domain.channel.Channel;
import com.example.inventory.domain.order.Order;
import com.example.inventory.dto.request.OrderRequest;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.repository.ChannelRepository;
import com.example.inventory.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ChannelRepository channelRepository;

    public OrderService(OrderRepository orderRepository, ChannelRepository channelRepository) {
        this.orderRepository = orderRepository;
        this.channelRepository = channelRepository;
    }

    @Transactional(readOnly = true)
    public boolean exists(OrderRequest request) {
        return orderRepository.findByExternalOrderIdAndChannelId(request.externalOrderId(), request.channelId()).isPresent();
    }

    @Transactional
    public Order createOrderShell(OrderRequest request) {
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NotFoundException("Channel not found for id " + request.channelId()));
        Order order = new Order();
        order.setExternalOrderId(request.externalOrderId());
        order.setChannel(channel);
        order.setTotalAmount(request.totalAmount());
        return orderRepository.save(order);
    }
}
