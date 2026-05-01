package com.example.inventory.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.channels")
public class ChannelIntegrationProperties {

    private Shopify shopify = new Shopify();
    private Amazon amazon = new Amazon();
    private Flipkart flipkart = new Flipkart();

    public Shopify getShopify() {
        return shopify;
    }

    public void setShopify(Shopify shopify) {
        this.shopify = shopify;
    }

    public Amazon getAmazon() {
        return amazon;
    }

    public void setAmazon(Amazon amazon) {
        this.amazon = amazon;
    }

    public Flipkart getFlipkart() {
        return flipkart;
    }

    public void setFlipkart(Flipkart flipkart) {
        this.flipkart = flipkart;
    }

    public static class Shopify {
        private boolean enabled;
        private String shop = "";
        private String accessToken = "";
        private String apiVersion = "2024-10";
        private String locationId = "";
        private String ordersStatus = "open";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getShop() {
            return shop;
        }

        public void setShop(String shop) {
            this.shop = shop;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getApiVersion() {
            return apiVersion;
        }

        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }

        public String getLocationId() {
            return locationId;
        }

        public void setLocationId(String locationId) {
            this.locationId = locationId;
        }

        public String getOrdersStatus() {
            return ordersStatus;
        }

        public void setOrdersStatus(String ordersStatus) {
            this.ordersStatus = ordersStatus;
        }
    }

    public static class Amazon {
        private boolean enabled;
        private String baseUrl = "https://sellingpartnerapi-na.amazon.com";
        private String accessToken = "";
        private String ordersPath = "/orders/v0/orders";
        private String inventoryPath = "/fba/inventory/v1/summaries";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getOrdersPath() {
            return ordersPath;
        }

        public void setOrdersPath(String ordersPath) {
            this.ordersPath = ordersPath;
        }

        public String getInventoryPath() {
            return inventoryPath;
        }

        public void setInventoryPath(String inventoryPath) {
            this.inventoryPath = inventoryPath;
        }
    }

    public static class Flipkart {
        private boolean enabled;
        private String baseUrl = "https://api.flipkart.net";
        private String accessToken = "";
        private String ordersPath = "/v3/shipments/orders";
        private String inventoryPath = "/v3/listings/inventory";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getOrdersPath() {
            return ordersPath;
        }

        public void setOrdersPath(String ordersPath) {
            this.ordersPath = ordersPath;
        }

        public String getInventoryPath() {
            return inventoryPath;
        }

        public void setInventoryPath(String inventoryPath) {
            this.inventoryPath = inventoryPath;
        }
    }
}
