package com.retailsystem.dto;

import java.math.BigDecimal;

/**
 * One product's row in an ABC inventory analysis. See
 * AbcAnalysisService for how abcClass/percentages are computed — the
 * short version: products are sorted by revenue (highest first), and a
 * product's class is decided by how much cumulative revenue came from
 * everything ranked above it. Class A = the small set of products driving
 * most of the revenue; Class C = the long tail, including anything with
 * zero sales in the period (dead stock).
 */
public class AbcAnalysisItemDTO {

    private int rank;
    private Long productId;
    private String productName;
    private String sku;
    private String category;
    private int unitsSold;
    private BigDecimal revenue;
    private BigDecimal revenuePercent;
    private BigDecimal cumulativeRevenuePercent;
    private String abcClass;
    private boolean hasSales;
    private int currentStock;
    private BigDecimal stockValue;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getUnitsSold() { return unitsSold; }
    public void setUnitsSold(int unitsSold) { this.unitsSold = unitsSold; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public BigDecimal getRevenuePercent() { return revenuePercent; }
    public void setRevenuePercent(BigDecimal revenuePercent) { this.revenuePercent = revenuePercent; }
    public BigDecimal getCumulativeRevenuePercent() { return cumulativeRevenuePercent; }
    public void setCumulativeRevenuePercent(BigDecimal cumulativeRevenuePercent) { this.cumulativeRevenuePercent = cumulativeRevenuePercent; }
    public String getAbcClass() { return abcClass; }
    public void setAbcClass(String abcClass) { this.abcClass = abcClass; }
    public boolean isHasSales() { return hasSales; }
    public void setHasSales(boolean hasSales) { this.hasSales = hasSales; }
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    public BigDecimal getStockValue() { return stockValue; }
    public void setStockValue(BigDecimal stockValue) { this.stockValue = stockValue; }
}
