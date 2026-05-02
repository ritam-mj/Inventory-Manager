package com.example.inventory.service.sync;

import com.example.inventory.domain.channel.Channel;
import com.example.inventory.dto.response.ChannelPollResultResponse;
import com.example.inventory.repository.ChannelRepository;
import com.example.inventory.service.channel.ChannelService;
import java.util.ArrayList;
import java.util.List;
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

    public List<ChannelPollResultResponse> pollOrdersFromChannels() {
        List<ChannelPollResultResponse> results = new ArrayList<>();
        channelService.adapters().forEach(adapter -> results.add(pollOrdersFromChannel(adapter.channelName())));
        return results;
    }

    public ChannelPollResultResponse pollOrdersFromChannel(String channelName) {
        Channel channel = channelRepository.findByNameIgnoreCase(channelName).orElse(null);
        if (channel == null) {
            return new ChannelPollResultResponse(
                    null,
                    channelName,
                    0,
                    "NOT_FOUND",
                    "Channel not found in database");
        }

        var adapterOpt = channelService.findAdapter(channelName);
        if (adapterOpt.isEmpty()) {
            return new ChannelPollResultResponse(
                    channel.getId(),
                    channel.getName(),
                    0,
                    "NO_ADAPTER",
                    "No adapter registered for channel");
        }

        try {
            int fetched = orderSyncService.pull(adapterOpt.get(), channel.getId());
            return new ChannelPollResultResponse(
                    channel.getId(),
                    channel.getName(),
                    fetched,
                    "SUCCESS",
                    "Order pull completed");
        } catch (Exception ex) {
            return new ChannelPollResultResponse(
                    channel.getId(),
                    channel.getName(),
                    0,
                    "FAILED",
                    ex.getMessage());
        }
    }
}
