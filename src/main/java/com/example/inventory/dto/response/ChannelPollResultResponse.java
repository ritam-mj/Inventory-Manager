package com.example.inventory.dto.response;

public record ChannelPollResultResponse(
        Long channelId,
        String channelName,
        int fetchedOrders,
        String status,
        String message
) {
}
