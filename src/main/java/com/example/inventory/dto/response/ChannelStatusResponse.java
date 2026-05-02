package com.example.inventory.dto.response;

public record ChannelStatusResponse(
        Long channelId,
        String channelName,
        boolean adapterRegistered,
        boolean enabled,
        boolean configured
) {
}
