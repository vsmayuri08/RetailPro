package com.retailsystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A full or partial return against a completed Sale. Created only via
 * SaleReturnService#processReturn, which restocks through
 * InventoryBatchService and reverses loyalty through
 * CustomerService#adjustLoyaltyPoints in the same transaction.
 */
@Entity
@Table(name = "sale_returns")
public class SaleReturn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(nullable = false)
    private int loyaltyPointsReversed = 0;

    @OneToMany(mappedBy = "saleReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleReturnItem> items = new ArrayList<>();

    public SaleReturn() {
    }

    public SaleReturn(Sale sale, User processedBy, String reason, BigDecimal refundAmount) {
        this.sale = sale;
        this.processedBy = processedBy;
        this.reason = reason;
        this.refundAmount = refundAmount;
    }

    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }
    public User getProcessedBy() { return processedBy; }
    public void setProcessedBy(User processedBy) { this.processedBy = processedBy; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public int getLoyaltyPointsReversed() { return loyaltyPointsReversed; }
    public void setLoyaltyPointsReversed(int loyaltyPointsReversed) { this.loyaltyPointsReversed = loyaltyPointsReversed; }
    public List<SaleReturnItem> getItems() { return items; }
    public void setItems(List<SaleReturnItem> items) { this.items = items; }
}
