package com.example.inventory.integration.shopify;

import com.example.inventory.dto.channel.ChannelOrderSnapshot;
import com.example.inventory.dto.request.OrderItemRequest;
import com.example.inventory.integration.common.ChannelAdapter;
import com.example.inventory.integration.common.ChannelRestExecutor;
import com.example.inventory.integration.common.SkuLookup;
import com.example.inventory.integration.config.ChannelIntegrationProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ShopifyAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(ShopifyAdapter.class);
    private static final String CHANNEL_KEY = "shopify";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final ChannelIntegrationProperties properties;
    private final ChannelRestExecutor channelRestExecutor;
    private final SkuLookup skuLookup;

    public ShopifyAdapter(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            ChannelIntegrationProperties properties,
            ChannelRestExecutor channelRestExecutor,
            SkuLookup skuLookup) {
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.channelRestExecutor = channelRestExecutor;
        this.skuLookup = skuLookup;
    }

    @Override
    public String channelName() {
        return CHANNEL_KEY;
    }

    @Override
    public List<ChannelOrderSnapshot> fetchOrders() {
        if (!properties.getShopify().isEnabled()) {
            return List.of();
        }
        var s = properties.getShopify();
        if (s.getShop().isBlank() || s.getAccessToken().isBlank()) {
            log.debug("Shopify fetch skipped: missing shop or access token");
            return List.of();
        }
        String base = adminBase(s);
        URI uri = UriComponentsBuilder.fromUriString(base + "/orders.json")
                .queryParam("status", s.getOrdersStatus())
                .queryParam("limit", 50)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        return channelRestExecutor.execute(CHANNEL_KEY, () -> {
            RestClient client = restClientBuilder.build();
            String body = client.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(h -> h.set("X-Shopify-Access-Token", s.getAccessToken()))
                    .retrieve()
                    .body(String.class);
            try {
                return parseOrders(body);
            } catch (Exception e) {
                throw new RuntimeException("Shopify order parse failed", e);
            }
        });
    }

    private List<ChannelOrderSnapshot> parseOrders(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode orders = root.get("orders");
        if (orders == null || !orders.isArray()) {
            return List.of();
        }
        List<ChannelOrderSnapshot> result = new ArrayList<>();
        for (JsonNode order : orders) {
            String id = order.hasNonNull("id")
                    ? order.get("id").asText()
                    : order.hasNonNull("name") ? order.get("name").asText() : null;
            if (id == null || id.isBlank()) {
                continue;
            }
            BigDecimal total = order.hasNonNull("total_price")
                    ? new BigDecimal(order.get("total_price").asText())
                    : BigDecimal.ZERO;
            List<OrderItemRequest> items = new ArrayList<>();
            JsonNode lineItems = order.get("line_items");
            if (lineItems != null && lineItems.isArray()) {
                for (JsonNode li : lineItems) {
                    String sku = li.hasNonNull("sku") ? li.get("sku").asText() : "";
                    var skuId = skuLookup.findIdByCode(sku);
                    if (skuId.isEmpty()) {
                        log.warn("Skipping Shopify line item: unknown sku {}", sku);
                        continue;
                    }
                    int qty = li.has("quantity") ? li.get("quantity").asInt(1) : 1;
                    BigDecimal price = li.hasNonNull("price")
                            ? new BigDecimal(li.get("price").asText())
                            : BigDecimal.ZERO;
                    items.add(new OrderItemRequest(skuId.get(), qty, price));
                }
            }
            if (!items.isEmpty()) {
                result.add(new ChannelOrderSnapshot(id, total, items));
            }
        }
        return result;
    }

    @Override
    public void updateInventory(String skuCode, int qty) {
        if (!properties.getShopify().isEnabled()) {
            return;
        }
        var s = properties.getShopify();
        if (s.getShop().isBlank() || s.getAccessToken().isBlank()) {
            return;
        }
        if (s.getLocationId() == null || s.getLocationId().isBlank()) {
            log.warn("Shopify inventory sync skipped: set app.channels.shopify.location-id");
            return;
        }
        channelRestExecutor.run(CHANNEL_KEY, () -> {
            try {
                RestClient client = restClientBuilder.build();
                String base = adminBase(s);
                URI variantUri = UriComponentsBuilder.fromUriString(base + "/variants.json")
                        .queryParam("sku", skuCode)
                        .build()
                        .encode(StandardCharsets.UTF_8)
                        .toUri();
                String variantBody = client.get()
                        .uri(variantUri)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(h -> h.set("X-Shopify-Access-Token", s.getAccessToken()))
                        .retrieve()
                        .body(String.class);
                JsonNode root = objectMapper.readTree(variantBody);
                JsonNode variants = root.get("variants");
                if (variants == null || !variants.isArray() || variants.size() == 0) {
                    log.warn("Shopify: no variant for sku {}", skuCode);
                    return;
                }
                long inventoryItemId = variants.get(0).get("inventory_item_id").asLong();
                long locationId = Long.parseLong(s.getLocationId());
                ObjectNode body = objectMapper.createObjectNode();
                body.put("location_id", locationId);
                body.put("inventory_item_id", inventoryItemId);
                body.put("available", qty);
                client.post()
                        .uri(URI.create(base + "/inventory_levels/set.json"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(h -> h.set("X-Shopify-Access-Token", s.getAccessToken()))
                        .body(body.toString())
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                throw new RuntimeException("Shopify inventory update failed", e);
            }
        });
    }

    @Override
    public void cancelOrder(String externalOrderId) {
        if (!properties.getShopify().isEnabled()) {
            return;
        }
        var s = properties.getShopify();
        if (s.getShop().isBlank() || s.getAccessToken().isBlank()) {
            return;
        }
        channelRestExecutor.run(CHANNEL_KEY, () -> {
            try {
                RestClient client = restClientBuilder.build();
                String base = adminBase(s);
                String payload = "{\"reason\":\"other\"}";
                client.post()
                        .uri(URI.create(base + "/orders/" + externalOrderId + "/cancel.json"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(h -> h.set("X-Shopify-Access-Token", s.getAccessToken()))
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                throw new RuntimeException("Shopify cancel failed", e);
            }
        });
    }

    private String adminBase(ChannelIntegrationProperties.Shopify s) {
        return "https://" + stripProtocol(s.getShop()) + "/admin/api/" + s.getApiVersion();
    }

    private static String stripProtocol(String shop) {
        String h = shop.trim();
        if (h.startsWith("https://")) {
            return h.substring(8);
        }
        if (h.startsWith("http://")) {
            return h.substring(7);
        }
        return h;
    }
}
