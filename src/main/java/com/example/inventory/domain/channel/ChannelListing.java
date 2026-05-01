package com.example.inventory.domain.channel;

import com.example.inventory.domain.product.Sku;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "channel_listing",
       uniqueConstraints = @UniqueConstraint(columnNames = {"sku_id", "channel_id"}))
public class ChannelListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id")
    private Sku sku;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(name = "external_listing_id", nullable = false)
    private String externalListingId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String status;

    public Long getId() { return id; }
    public Sku getSku() { return sku; }
    public void setSku(Sku sku) { this.sku = sku; }
    public Channel getChannel() { return channel; }
    public void setChannel(Channel channel) { this.channel = channel; }
    public String getExternalListingId() { return externalListingId; }
    public void setExternalListingId(String externalListingId) { this.externalListingId = externalListingId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
