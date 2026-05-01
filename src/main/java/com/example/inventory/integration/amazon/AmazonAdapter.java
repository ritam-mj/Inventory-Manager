package com.example.inventory.integration.amazon;

import com.example.inventory.dto.channel.ChannelOrderSnapshot;
import com.example.inventory.dto.request.OrderItemRequest;
import com.example.inventory.integration.common.ChannelAdapter;
import com.example.inventory.integration.common.ChannelRestExecutor;
import com.example.inventory.integration.common.SkuLookup;
import com.example.inventory.integration.config.ChannelIntegrationProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for order/inventory endpoints. Production SP-API typically requires AWS SigV4
 * in addition to LWA tokens; configure {@code base-url} to a gateway or extend signing here.
 */
@Component
public class AmazonAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(AmazonAdapter.class);
    private static final String CHANNEL_KEY = "amazon";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final ChannelIntegrationProperties properties;
    private final ChannelRestExecutor channelRestExecutor;
    private final SkuLookup skuLookup;

    public AmazonAdapter(
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
        if (!properties.getAmazon().isEnabled()) {
            return List.of();
        }
        var a = properties.getAmazon();
        if (a.getAccessToken().isBlank()) {
            log.debug("Amazon fetch skipped: missing access token");
            return List.of();
        }
        URI uri = URI.create(joinUrl(a.getBaseUrl(), a.getOrdersPath()));

        return channelRestExecutor.execute(CHANNEL_KEY, () -> {
            RestClient client = restClientBuilder.build();
            String body = client.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(h -> h.set("x-amz-access-token", a.getAccessToken()))
                    .retrieve()
                    .body(String.class);
            try {
                return parseOrders(body);
            } catch (Exception e) {
                throw new RuntimeException("Amazon order parse failed", e);
            }
        });
    }

    private List<ChannelOrderSnapshot> parseOrders(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode orders = root.path("payload").path("Orders");
        if (!orders.isArray()) {
            orders = root.path("orders");
        }
        if (!orders.isArray()) {
            return List.of();
        }
        List<ChannelOrderSnapshot> result = new ArrayList<>();
        for (JsonNode order : orders) {
            String id = textOrNull(order, "AmazonOrderId", "orderId", "id");
            if (id == null) {
                continue;
            }
            BigDecimal total = BigDecimal.ZERO;
            if (order.has("OrderTotal")) {
                total = new BigDecimal(order.path("OrderTotal").path("Amount").asText("0"));
            } else if (order.hasNonNull("total")) {
                total = new BigDecimal(order.get("total").asText());
            }
            List<OrderItemRequest> items = new ArrayList<>();
            JsonNode orderItems = order.path("OrderItems");
            if (!orderItems.isArray()) {
                orderItems = order.path("items");
            }
            if (orderItems.isArray()) {
                for (JsonNode oi : orderItems) {
                    String sku = textOrNull(oi, "SellerSKU", "sku", "sellerSku");
                    var skuId = skuLookup.findIdByCode(sku == null ? "" : sku);
                    if (skuId.isEmpty()) {
                        log.warn("Skipping Amazon line item: unknown sku {}", sku);
                        continue;
                    }
                    int qty = oi.has("QuantityOrdered") ? oi.get("QuantityOrdered").asInt(1) : oi.path("quantity").asInt(1);
                    BigDecimal price = BigDecimal.ZERO;
                    if (oi.has("ItemPrice")) {
                        price = new BigDecimal(oi.path("ItemPrice").path("Amount").asText("0"));
                    } else if (oi.hasNonNull("price")) {
                        price = new BigDecimal(oi.get("price").asText());
                    }
                    items.add(new OrderItemRequest(skuId.get(), qty, price));
                }
            }
            if (!items.isEmpty()) {
                result.add(new ChannelOrderSnapshot(id, total, items));
            }
        }
        return result;
    }

    private static String textOrNull(JsonNode node, String... fieldNames) {
        for (String f : fieldNames) {
            if (node.hasNonNull(f)) {
                return node.get(f).asText();
            }
        }
        return null;
    }

    @Override
    public void updateInventory(String skuCode, int qty) {
        if (!properties.getAmazon().isEnabled() || properties.getAmazon().getAccessToken().isBlank()) {
            return;
        }
        var a = properties.getAmazon();
        URI uri = URI.create(joinUrl(a.getBaseUrl(), a.getInventoryPath()));
        String payload = objectMapper.createObjectNode()
                .put("sku", skuCode)
                .put("availableQuantity", qty)
                .toString();

        channelRestExecutor.run(CHANNEL_KEY, () -> {
            RestClient client = restClientBuilder.build();
            client.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> h.set("x-amz-access-token", a.getAccessToken()))
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        });
    }

    @Override
    public void cancelOrder(String externalOrderId) {
        if (!properties.getAmazon().isEnabled() || properties.getAmazon().getAccessToken().isBlank()) {
            return;
        }
        log.debug("Amazon cancel not implemented for order {} (use Orders API / cancel feed)", externalOrderId);
    }

    private static String joinUrl(String baseUrl, String path) {
        String b = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String p = path.startsWith("/") ? path : "/" + path;
        return b + p;
    }
}
