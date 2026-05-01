-- Central schema (MySQL-oriented DDL; H2 runs with MODE=MySQL for dev)
-- JSON columns use VARCHAR for broad H2/MySQL compatibility; map as string in JPA.

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku_code VARCHAR(100) NOT NULL UNIQUE,
    attributes VARCHAR(2000),
    CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL UNIQUE,
    available_qty INT NOT NULL DEFAULT 0,
    reserved_qty INT NOT NULL DEFAULT 0,
    safety_stock INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_sku FOREIGN KEY (sku_id) REFERENCES sku(id)
);

CREATE TABLE channel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    config VARCHAR(2000)
);

CREATE TABLE channel_listing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    external_listing_id VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_listing_sku FOREIGN KEY (sku_id) REFERENCES sku(id),
    CONSTRAINT fk_listing_channel FOREIGN KEY (channel_id) REFERENCES channel(id),
    CONSTRAINT uq_listing_sku_channel UNIQUE (sku_id, channel_id)
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_order_id VARCHAR(100) NOT NULL,
    channel_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_channel FOREIGN KEY (channel_id) REFERENCES channel(id),
    CONSTRAINT uq_external_order_channel UNIQUE (external_order_id, channel_id)
);

CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_sku FOREIGN KEY (sku_id) REFERENCES sku(id)
);

CREATE TABLE inventory_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    order_item_id BIGINT,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_sku FOREIGN KEY (sku_id) REFERENCES sku(id),
    CONSTRAINT fk_reservation_order_item FOREIGN KEY (order_item_id) REFERENCES order_item(id)
);

CREATE TABLE sync_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payload VARCHAR(4000),
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
