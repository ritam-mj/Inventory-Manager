package com.example.inventory.service.channel;

import com.example.inventory.integration.common.ChannelAdapter;
import java.util.List;
import java.util.Optional;
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

    public Optional<ChannelAdapter> findAdapter(String channelName) {
        return adapters.stream()
                .filter(a -> a.channelName().equalsIgnoreCase(channelName))
                .findFirst();
    }
}
