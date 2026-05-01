package com.example.inventory.repository;

import com.example.inventory.domain.inventory.Inventory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySkuId(Long skuId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findWithLockBySkuId(Long skuId);
}
