package com.retailsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BillingSettingsRequest {

    @NotNull(message = "Tax rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Tax rate cannot be negative")
    private BigDecimal taxRatePercent;

    @NotNull(message = "Loyalty points per amount is required")
    @Min(value = 0, message = "Loyalty points cannot be negative")
    private Integer loyaltyPointsPerAmount;

    @NotNull(message = "Loyalty amount threshold is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Loyalty amount threshold must be greater than zero")
    private BigDecimal loyaltyAmountThreshold;

    public BigDecimal getTaxRatePercent() { return taxRatePercent; }
    public void setTaxRatePercent(BigDecimal taxRatePercent) { this.taxRatePercent = taxRatePercent; }
    public Integer getLoyaltyPointsPerAmount() { return loyaltyPointsPerAmount; }
    public void setLoyaltyPointsPerAmount(Integer loyaltyPointsPerAmount) { this.loyaltyPointsPerAmount = loyaltyPointsPerAmount; }
    public BigDecimal getLoyaltyAmountThreshold() { return loyaltyAmountThreshold; }
    public void setLoyaltyAmountThreshold(BigDecimal loyaltyAmountThreshold) { this.loyaltyAmountThreshold = loyaltyAmountThreshold; }
}
