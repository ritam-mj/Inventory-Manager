package com.example.inventory.service.sync;

import com.example.inventory.domain.channel.Channel;
import com.example.inventory.repository.ChannelRepository;
import com.example.inventory.service.channel.ChannelService;
import org.springframework.stereotype.Service;

@Service
public class SyncOrchestrator {

    private final ChannelService channelService;
    private final ChannelRepository channelRepository;
    private final OrderSyncService orderSyncService;

    public SyncOrchestrator(
            ChannelService channelService,
            ChannelRepository channelRepository,
            OrderSyncService orderSyncService) {
        this.channelService = channelService;
        this.channelRepository = channelRepository;
        this.orderSyncService = orderSyncService;
    }

    public void pollOrdersFromChannels() {
        channelService.adapters().forEach(adapter -> {
            Channel channel = channelRepository.findByNameIgnoreCase(adapter.channelName()).orElse(null);
            if (channel != null) {
                orderSyncService.pull(adapter, channel.getId());
            }
        });
    }
}
