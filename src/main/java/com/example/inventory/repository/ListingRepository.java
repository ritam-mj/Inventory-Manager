package com.example.inventory.repository;

import com.example.inventory.domain.channel.ChannelListing;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<ChannelListing, Long> {
    List<ChannelListing> findBySkuId(Long skuId);
}
