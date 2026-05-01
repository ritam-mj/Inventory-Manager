package com.example.inventory.integration.common;

import com.example.inventory.domain.product.Sku;
import com.example.inventory.repository.SkuRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SkuLookup {

    private final SkuRepository skuRepository;

    public SkuLookup(SkuRepository skuRepository) {
        this.skuRepository = skuRepository;
    }

    public Optional<Long> findIdByCode(String skuCode) {
        if (skuCode == null || skuCode.isBlank()) {
            return Optional.empty();
        }
        return skuRepository.findBySkuCode(skuCode.trim()).map(Sku::getId);
    }
}
