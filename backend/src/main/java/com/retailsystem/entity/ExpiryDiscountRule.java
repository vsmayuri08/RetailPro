package com.retailsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Module 10 -> one tier of the expiry-based discount ladder, e.g.
 * "15 to 30 days before expiry -> 10% off". Rows are managed entirely by
 * the Super Admin through ExpiryDiscountController — nothing about the
 * thresholds or percentages is hard-coded in Java; see
 * ExpiryDiscountService#computeDiscountPercent, which just reads whatever
 * active rows exist. A product further than every rule's range from
 * expiry (or with no expiry date at all) simply gets no discount; an
 * already-expired product is blocked from sale entirely, which is
 * enforced separately in SaleService and is not a "discount" case.
 */
@Entity
@Table(name = "expiry_discount_rules")
public class ExpiryDiscountRule extends BaseEntity {

    private String label;

    @Column(nullable = false)
    private int minDaysBeforeExpiry;

    @Column(nullable = false)
    private int maxDaysBeforeExpiry;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(nullable = false)
    private boolean active = true;

    public ExpiryDiscountRule() {
    }

    public ExpiryDiscountRule(String label, int minDaysBeforeExpiry, int maxDaysBeforeExpiry, BigDecimal discountPercent) {
        this.label = label;
        this.minDaysBeforeExpiry = minDaysBeforeExpiry;
        this.maxDaysBeforeExpiry = maxDaysBeforeExpiry;
        this.discountPercent = discountPercent;
        this.active = true;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getMinDaysBeforeExpiry() { return minDaysBeforeExpiry; }
    public void setMinDaysBeforeExpiry(int minDaysBeforeExpiry) { this.minDaysBeforeExpiry = minDaysBeforeExpiry; }
    public int getMaxDaysBeforeExpiry() { return maxDaysBeforeExpiry; }
    public void setMaxDaysBeforeExpiry(int maxDaysBeforeExpiry) { this.maxDaysBeforeExpiry = maxDaysBeforeExpiry; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
