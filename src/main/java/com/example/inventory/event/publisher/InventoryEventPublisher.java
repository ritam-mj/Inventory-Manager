package com.example.inventory.event.publisher;

import com.example.inventory.event.InventoryChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private final ApplicationEventPublisher publisher;

    public InventoryEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishInventoryChanged(Long skuId) {
        publisher.publishEvent(new InventoryChangedEvent(skuId));
    }
}
