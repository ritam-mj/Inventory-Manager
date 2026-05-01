package com.example.inventory.service.inventory;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AllocationService {

    public Map<String, Integer> allocateAcrossChannels(int sellableQty, Map<String, Integer> channelWeights) {
        int totalWeight = channelWeights.values().stream().mapToInt(Integer::intValue).sum();
        return channelWeights.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (sellableQty * e.getValue()) / Math.max(totalWeight, 1)
                ));
    }
}
