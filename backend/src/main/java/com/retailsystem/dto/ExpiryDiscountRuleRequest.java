package com.retailsystem.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ExpiryDiscountRuleRequest {

    private String label;

    @NotNull(message = "Minimum days before expiry is required")
    @Min(value = 0, message = "Minimum days cannot be negative")
    private Integer minDaysBeforeExpiry;

    @NotNull(message = "Maximum days before expiry is required")
    @Min(value = 0, message = "Maximum days cannot be negative")
    private Integer maxDaysBeforeExpiry;

    @NotNull(message = "Discount percent is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", inclusive = true, message = "Discount cannot exceed 100%")
    private BigDecimal discountPercent;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getMinDaysBeforeExpiry() { return minDaysBeforeExpiry; }
    public void setMinDaysBeforeExpiry(Integer minDaysBeforeExpiry) { this.minDaysBeforeExpiry = minDaysBeforeExpiry; }
    public Integer getMaxDaysBeforeExpiry() { return maxDaysBeforeExpiry; }
    public void setMaxDaysBeforeExpiry(Integer maxDaysBeforeExpiry) { this.maxDaysBeforeExpiry = maxDaysBeforeExpiry; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
}
