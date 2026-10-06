package com.retailsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Singleton settings row controlling checkout math. There is always exactly
 * one row (id 1) — see BillingSettingsService, which creates it with
 * defaults on first access if missing.
 *
 * Per explicit request: loyalty points are NOT a hard-coded rate. A sale
 * earns {@code loyaltyPointsPerAmount} points for every
 * {@code loyaltyAmountThreshold} spent, and the Super Admin edits both
 * numbers from the Billing Settings screen — nothing about the earn rate
 * is fixed in code. (Note: this is separate from the loyalty *tier*
 * thresholds in CustomerService, which are the Bronze/Silver/Gold/Platinum
 * point cutoffs — a customer's points feed into those tiers however this
 * rate produces them.)
 */
@Entity
@Table(name = "billing_settings")
public class BillingSettings extends BaseEntity {

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRatePercent = new BigDecimal("5.00");

    @Column(nullable = false)
    private int loyaltyPointsPerAmount = 1;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal loyaltyAmountThreshold = new BigDecimal("100.00");

    public BillingSettings() {
    }

    public BigDecimal getTaxRatePercent() { return taxRatePercent; }
    public void setTaxRatePercent(BigDecimal taxRatePercent) { this.taxRatePercent = taxRatePercent; }
    public int getLoyaltyPointsPerAmount() { return loyaltyPointsPerAmount; }
    public void setLoyaltyPointsPerAmount(int loyaltyPointsPerAmount) { this.loyaltyPointsPerAmount = loyaltyPointsPerAmount; }
    public BigDecimal getLoyaltyAmountThreshold() { return loyaltyAmountThreshold; }
    public void setLoyaltyAmountThreshold(BigDecimal loyaltyAmountThreshold) { this.loyaltyAmountThreshold = loyaltyAmountThreshold; }
}
