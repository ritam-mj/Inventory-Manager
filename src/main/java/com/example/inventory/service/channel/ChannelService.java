package com.example.inventory.service.channel;

import com.example.inventory.integration.common.ChannelAdapter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ChannelService {

    private final List<ChannelAdapter> adapters;

    public ChannelService(List<ChannelAdapter> adapters) {
        this.adapters = adapters;
    }

    public List<ChannelAdapter> adapters() {
        return adapters;
    }
}
