package com.example.inventory.integration.common;

import com.example.inventory.dto.channel.ChannelOrderSnapshot;
import java.util.List;

public interface ChannelAdapter {

    String channelName();

    List<ChannelOrderSnapshot> fetchOrders();

    void updateInventory(String skuCode, int qty);

    void cancelOrder(String externalOrderId);
}
