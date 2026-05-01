package com.example.inventory.service.inventory;

import com.example.inventory.domain.inventory.Inventory;
import com.example.inventory.domain.inventory.InventoryReservation;
import com.example.inventory.domain.inventory.ReservationStatus;
import com.example.inventory.domain.order.OrderItem;
import com.example.inventory.domain.product.Sku;
import com.example.inventory.event.publisher.InventoryEventPublisher;
import com.example.inventory.exception.NotFoundException;
import com.example.inventory.exception.OutOfStockException;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ReservationRepository;
import com.example.inventory.repository.SkuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    private final SkuRepository skuRepository;
    private final InventoryEventPublisher inventoryEventPublisher;

    public ReservationService(
            InventoryRepository inventoryRepository,
            ReservationRepository reservationRepository,
            SkuRepository skuRepository,
            InventoryEventPublisher inventoryEventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.skuRepository = skuRepository;
        this.inventoryEventPublisher = inventoryEventPublisher;
    }

    @Transactional
    public void reserveStock(Long skuId, int qty, OrderItem orderItem) {
        Inventory inv = inventoryRepository.findWithLockBySkuId(skuId)
                .orElseThrow(() -> new NotFoundException("Inventory not found for sku " + skuId));

        int available = inv.getAvailableQty() - inv.getReservedQty();
        if (available < qty) {
            throw new OutOfStockException("Out of stock for sku " + skuId);
        }

        inv.setReservedQty(inv.getReservedQty() + qty);
        inventoryRepository.save(inv);

        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new NotFoundException("SKU not found for id " + skuId));

        InventoryReservation reservation = new InventoryReservation();
        reservation.setSku(sku);
        reservation.setOrderItem(orderItem);
        reservation.setQuantity(qty);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);

        inventoryEventPublisher.publishInventoryChanged(skuId);
    }
}
