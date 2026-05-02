package com.example.inventory.controller;

import com.example.inventory.dto.response.ChannelPollResultResponse;
import com.example.inventory.dto.response.ChannelStatusResponse;
import com.example.inventory.integration.config.ChannelIntegrationProperties;
import com.example.inventory.repository.ChannelRepository;
import com.example.inventory.service.channel.ChannelService;
import com.example.inventory.service.sync.SyncOrchestrator;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;
    private final ChannelRepository channelRepository;
    private final ChannelIntegrationProperties integrationProperties;
    private final SyncOrchestrator syncOrchestrator;

    public ChannelController(
            ChannelService channelService,
            ChannelRepository channelRepository,
            ChannelIntegrationProperties integrationProperties,
            SyncOrchestrator syncOrchestrator) {
        this.channelService = channelService;
        this.channelRepository = channelRepository;
        this.integrationProperties = integrationProperties;
        this.syncOrchestrator = syncOrchestrator;
    }

    @GetMapping
    public List<String> list() {
        return channelService.adapters().stream().map(a -> a.channelName()).toList();
    }

    @GetMapping("/status")
    public List<ChannelStatusResponse> status() {
        return channelRepository.findAll().stream()
                .map(ch -> {
                    String name = ch.getName().toLowerCase();
                    boolean enabled = isEnabled(name);
                    boolean configured = isConfigured(name);
                    boolean adapterRegistered = channelService.findAdapter(name).isPresent();
                    return new ChannelStatusResponse(
                            ch.getId(),
                            ch.getName(),
                            adapterRegistered,
                            enabled,
                            configured);
                })
                .toList();
    }

    @PostMapping("/poll-orders")
    public List<ChannelPollResultResponse> pollOrders() {
        return syncOrchestrator.pollOrdersFromChannels();
    }

    @PostMapping("/{channelName}/poll-orders")
    public ChannelPollResultResponse pollOrdersByChannel(@PathVariable String channelName) {
        return syncOrchestrator.pollOrdersFromChannel(channelName);
    }

    private boolean isEnabled(String name) {
        return switch (name) {
            case "shopify" -> integrationProperties.getShopify().isEnabled();
            case "amazon" -> integrationProperties.getAmazon().isEnabled();
            case "flipkart" -> integrationProperties.getFlipkart().isEnabled();
            default -> false;
        };
    }

    private boolean isConfigured(String name) {
        return switch (name) {
            case "shopify" -> !integrationProperties.getShopify().getShop().isBlank()
                    && !integrationProperties.getShopify().getAccessToken().isBlank();
            case "amazon" -> !integrationProperties.getAmazon().getAccessToken().isBlank();
            case "flipkart" -> !integrationProperties.getFlipkart().getAccessToken().isBlank();
            default -> false;
        };
    }
}
