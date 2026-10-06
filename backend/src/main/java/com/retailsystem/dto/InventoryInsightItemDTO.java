package com.retailsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryInsightItemDTO {

    private Long productId;
    private String productName;
    private String sku;
    private int currentStock;
    private BigDecimal averageDailySales;
    private BigDecimal predictedDemand;
    private Integer daysOfStockRemaining;
    private String riskLevel;
    private String insightType;
    private String headline;
    private String explanation;
    private LocalDate expiryDate;
    private boolean hasSales;
    private String trend;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    public BigDecimal getAverageDailySales() { return averageDailySales; }
    public void setAverageDailySales(BigDecimal averageDailySales) { this.averageDailySales = averageDailySales; }
    public BigDecimal getPredictedDemand() { return predictedDemand; }
    public void setPredictedDemand(BigDecimal predictedDemand) { this.predictedDemand = predictedDemand; }
    public Integer getDaysOfStockRemaining() { return daysOfStockRemaining; }
    public void setDaysOfStockRemaining(Integer daysOfStockRemaining) { this.daysOfStockRemaining = daysOfStockRemaining; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getInsightType() { return insightType; }
    public void setInsightType(String insightType) { this.insightType = insightType; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public boolean isHasSales() { return hasSales; }
    public void setHasSales(boolean hasSales) { this.hasSales = hasSales; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
}
