package com.retailsystem.dto;

import com.retailsystem.entity.ExpiryDiscountRule;

import java.math.BigDecimal;

public class ExpiryDiscountRuleDTO {

    private Long id;
    private String label;
    private int minDaysBeforeExpiry;
    private int maxDaysBeforeExpiry;
    private BigDecimal discountPercent;
    private boolean active;

    public static ExpiryDiscountRuleDTO fromEntity(ExpiryDiscountRule rule) {
        ExpiryDiscountRuleDTO dto = new ExpiryDiscountRuleDTO();
        dto.id = rule.getId();
        dto.label = rule.getLabel();
        dto.minDaysBeforeExpiry = rule.getMinDaysBeforeExpiry();
        dto.maxDaysBeforeExpiry = rule.getMaxDaysBeforeExpiry();
        dto.discountPercent = rule.getDiscountPercent();
        dto.active = rule.isActive();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
