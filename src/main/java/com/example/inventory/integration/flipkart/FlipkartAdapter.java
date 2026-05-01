package com.example.inventory.integration.flipkart;

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

@Component
public class FlipkartAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(FlipkartAdapter.class);
    private static final String CHANNEL_KEY = "flipkart";

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final ChannelIntegrationProperties properties;
    private final ChannelRestExecutor channelRestExecutor;
    private final SkuLookup skuLookup;

    public FlipkartAdapter(
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
        if (!properties.getFlipkart().isEnabled()) {
            return List.of();
        }
        var f = properties.getFlipkart();
        if (f.getAccessToken().isBlank()) {
            log.debug("Flipkart fetch skipped: missing access token");
            return List.of();
        }
        URI uri = URI.create(joinUrl(f.getBaseUrl(), f.getOrdersPath()));

        return channelRestExecutor.execute(CHANNEL_KEY, () -> {
            RestClient client = restClientBuilder.build();
            String body = client.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(h -> {
                        h.setBearerAuth(f.getAccessToken());
                    })
                    .retrieve()
                    .body(String.class);
            try {
                return parseOrders(body);
            } catch (Exception e) {
                throw new RuntimeException("Flipkart order parse failed", e);
            }
        });
    }

    private List<ChannelOrderSnapshot> parseOrders(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode orders = root.path("orders");
        if (!orders.isArray()) {
            orders = root.path("payload").path("orders");
        }
        if (!orders.isArray()) {
            return List.of();
        }
        List<ChannelOrderSnapshot> result = new ArrayList<>();
        for (JsonNode order : orders) {
            String id = order.hasNonNull("orderId")
                    ? order.get("orderId").asText()
                    : order.hasNonNull("id") ? order.get("id").asText() : null;
            if (id == null || id.isBlank()) {
                continue;
            }
            BigDecimal total = order.hasNonNull("totalAmount")
                    ? new BigDecimal(order.get("totalAmount").asText())
                    : order.hasNonNull("total")
                            ? new BigDecimal(order.get("total").asText())
                            : BigDecimal.ZERO;
            List<OrderItemRequest> items = new ArrayList<>();
            JsonNode lines = order.path("orderItems");
            if (!lines.isArray()) {
                lines = order.path("items");
            }
            if (lines.isArray()) {
                for (JsonNode li : lines) {
                    String sku = li.hasNonNull("sku") ? li.get("sku").asText() : li.path("listingId").asText("");
                    var skuId = skuLookup.findIdByCode(sku);
                    if (skuId.isEmpty()) {
                        log.warn("Skipping Flipkart line item: unknown sku {}", sku);
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
        if (!properties.getFlipkart().isEnabled() || properties.getFlipkart().getAccessToken().isBlank()) {
            return;
        }
        var f = properties.getFlipkart();
        URI uri = URI.create(joinUrl(f.getBaseUrl(), f.getInventoryPath()));
        String payload = objectMapper.createObjectNode()
                .put("sku", skuCode)
                .put("quantity", qty)
                .toString();

        channelRestExecutor.run(CHANNEL_KEY, () -> {
            RestClient client = restClientBuilder.build();
            client.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> h.setBearerAuth(f.getAccessToken()))
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        });
    }

    @Override
    public void cancelOrder(String externalOrderId) {
        if (!properties.getFlipkart().isEnabled()) {
            return;
        }
        log.debug("Flipkart cancel not implemented for order {} (wire DELETE/POST per seller API)", externalOrderId);
    }

    private static String joinUrl(String baseUrl, String path) {
        String b = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String p = path.startsWith("/") ? path : "/" + path;
        return b + p;
    }
}
