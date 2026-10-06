package com.retailsystem.dto;

import java.time.LocalDate;
import java.util.List;

public class DemandForecastSummaryDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int lookbackDays;
    private int forecastDays;
    private int totalProducts;
    private int stockoutRiskCount;
    private int risingCount;
    private int fallingCount;
    private int stableCount;
    private int productsWithNoSales;
    private List<DemandForecastItemDTO> items;

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public int getLookbackDays() { return lookbackDays; }
    public void setLookbackDays(int lookbackDays) { this.lookbackDays = lookbackDays; }
    public int getForecastDays() { return forecastDays; }
    public void setForecastDays(int forecastDays) { this.forecastDays = forecastDays; }
    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
    public int getStockoutRiskCount() { return stockoutRiskCount; }
    public void setStockoutRiskCount(int stockoutRiskCount) { this.stockoutRiskCount = stockoutRiskCount; }
    public int getRisingCount() { return risingCount; }
    public void setRisingCount(int risingCount) { this.risingCount = risingCount; }
    public int getFallingCount() { return fallingCount; }
    public void setFallingCount(int fallingCount) { this.fallingCount = fallingCount; }
    public int getStableCount() { return stableCount; }
    public void setStableCount(int stableCount) { this.stableCount = stableCount; }
    public int getProductsWithNoSales() { return productsWithNoSales; }
    public void setProductsWithNoSales(int productsWithNoSales) { this.productsWithNoSales = productsWithNoSales; }
    public List<DemandForecastItemDTO> getItems() { return items; }
    public void setItems(List<DemandForecastItemDTO> items) { this.items = items; }
}
