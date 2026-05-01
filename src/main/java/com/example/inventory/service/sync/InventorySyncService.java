package com.example.inventory.service.sync;

import com.example.inventory.domain.channel.ChannelListing;
import com.example.inventory.domain.sync.SyncLog;
import com.example.inventory.domain.sync.SyncStatus;
import com.example.inventory.domain.sync.SyncType;
import com.example.inventory.integration.common.ChannelAdapter;
import com.example.inventory.repository.ListingRepository;
import com.example.inventory.repository.SyncLogRepository;
import com.example.inventory.service.channel.ChannelService;
import com.example.inventory.service.inventory.InventoryService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventorySyncService {

    private final InventoryService inventoryService;
    private final ListingRepository listingRepository;
    private final ChannelService channelService;
    private final SyncLogRepository syncLogRepository;

    public InventorySyncService(
            InventoryService inventoryService,
            ListingRepository listingRepository,
            ChannelService channelService,
            SyncLogRepository syncLogRepository) {
        this.inventoryService = inventoryService;
        this.listingRepository = listingRepository;
        this.channelService = channelService;
        this.syncLogRepository = syncLogRepository;
    }

    @Transactional
    public void syncInventory(Long skuId) {
        int sellableQty = inventoryService.getSellableQty(skuId);
        List<ChannelListing> listings = listingRepository.findBySkuId(skuId);

        for (ChannelListing listing : listings) {
            ChannelAdapter adapter = channelService.adapters().stream()
                    .filter(a -> a.channelName().equalsIgnoreCase(listing.getChannel().getName()))
                    .findFirst()
                    .orElse(null);
            if (adapter == null) {
                continue;
            }
            SyncLog log = new SyncLog();
            log.setType(SyncType.INVENTORY);
            log.setPayload("skuId=" + skuId + ",channel=" + adapter.channelName() + ",qty=" + sellableQty);
            try {
                adapter.updateInventory(listing.getSku().getSkuCode(), sellableQty);
                log.setStatus(SyncStatus.SUCCESS);
            } catch (Exception e) {
                log.setStatus(SyncStatus.FAILED);
            }
            syncLogRepository.save(log);
        }
    }
}
