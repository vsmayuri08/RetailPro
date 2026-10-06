package com.retailsystem.dto;

import com.retailsystem.entity.BillingSettings;

import java.math.BigDecimal;

public class BillingSettingsDTO {

    private BigDecimal taxRatePercent;
    private int loyaltyPointsPerAmount;
    private BigDecimal loyaltyAmountThreshold;

    public static BillingSettingsDTO fromEntity(BillingSettings settings) {
        BillingSettingsDTO dto = new BillingSettingsDTO();
        dto.taxRatePercent = settings.getTaxRatePercent();
        dto.loyaltyPointsPerAmount = settings.getLoyaltyPointsPerAmount();
        dto.loyaltyAmountThreshold = settings.getLoyaltyAmountThreshold();
        return dto;
    }

    public BigDecimal getTaxRatePercent() { return taxRatePercent; }
    public void setTaxRatePercent(BigDecimal taxRatePercent) { this.taxRatePercent = taxRatePercent; }
    public int getLoyaltyPointsPerAmount() { return loyaltyPointsPerAmount; }
    public void setLoyaltyPointsPerAmount(int loyaltyPointsPerAmount) { this.loyaltyPointsPerAmount = loyaltyPointsPerAmount; }
    public BigDecimal getLoyaltyAmountThreshold() { return loyaltyAmountThreshold; }
    public void setLoyaltyAmountThreshold(BigDecimal loyaltyAmountThreshold) { this.loyaltyAmountThreshold = loyaltyAmountThreshold; }
}
