package com.example.inventory.repository;

import com.example.inventory.domain.inventory.InventoryReservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<InventoryReservation, Long> {
    List<InventoryReservation> findByOrderItemOrderId(Long orderId);
}
