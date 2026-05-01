package com.example.inventory.repository;

import com.example.inventory.domain.product.Sku;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<Sku, Long> {
    Optional<Sku> findBySkuCode(String skuCode);
}
