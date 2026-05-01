package com.example.inventory.repository;

import com.example.inventory.domain.order.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByExternalOrderIdAndChannelId(String externalOrderId, Long channelId);
}
