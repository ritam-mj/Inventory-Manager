package com.example.inventory.controller;

import com.example.inventory.service.channel.ChannelService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping
    public List<String> list() {
        return channelService.adapters().stream().map(a -> a.channelName()).toList();
    }
}
