package com.retailsystem.dto;

import java.math.BigDecimal;

public class AbcClassSummaryDTO {

    private String abcClass;
    private int itemCount;
    private BigDecimal itemCountPercent;
    private BigDecimal revenue;
    private BigDecimal revenuePercent;
    private BigDecimal stockValue;

    public String getAbcClass() { return abcClass; }
    public void setAbcClass(String abcClass) { this.abcClass = abcClass; }
    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
    public BigDecimal getItemCountPercent() { return itemCountPercent; }
    public void setItemCountPercent(BigDecimal itemCountPercent) { this.itemCountPercent = itemCountPercent; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public BigDecimal getRevenuePercent() { return revenuePercent; }
    public void setRevenuePercent(BigDecimal revenuePercent) { this.revenuePercent = revenuePercent; }
    public BigDecimal getStockValue() { return stockValue; }
    public void setStockValue(BigDecimal stockValue) { this.stockValue = stockValue; }
}
