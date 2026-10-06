package com.retailsystem.dto;

import java.math.BigDecimal;

/**
 * One product's row in a demand forecast. See DemandForecastService for
 * how the moving-average forecast, trend label, and days-until-stockout
 * are computed — the short version: average daily units over the lookback
 * window are projected across the next N days and compared to current
 * stock. Zero sales in the window is kept as a row (not discarded): no
 * demand is itself useful information.
 */
public class DemandForecastItemDTO {

    private Long productId;
    private String productName;
    private String sku;
    private String category;
    private int unitsSold;
    private BigDecimal averageDailyUnitsSold;
    private BigDecimal forecastedDemand;
    private String trend;
    private int currentStock;
    private Integer estimatedDaysUntilStockout;
    private boolean stockoutRisk;
    private boolean hasSales;

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
    public BigDecimal getAverageDailyUnitsSold() { return averageDailyUnitsSold; }
    public void setAverageDailyUnitsSold(BigDecimal averageDailyUnitsSold) { this.averageDailyUnitsSold = averageDailyUnitsSold; }
    public BigDecimal getForecastedDemand() { return forecastedDemand; }
    public void setForecastedDemand(BigDecimal forecastedDemand) { this.forecastedDemand = forecastedDemand; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    public Integer getEstimatedDaysUntilStockout() { return estimatedDaysUntilStockout; }
    public void setEstimatedDaysUntilStockout(Integer estimatedDaysUntilStockout) { this.estimatedDaysUntilStockout = estimatedDaysUntilStockout; }
    public boolean isStockoutRisk() { return stockoutRisk; }
    public void setStockoutRisk(boolean stockoutRisk) { this.stockoutRisk = stockoutRisk; }
    public boolean isHasSales() { return hasSales; }
    public void setHasSales(boolean hasSales) { this.hasSales = hasSales; }
}
