package com.retailsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReorderSuggestionsSummaryDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int lookbackDays;
    private int totalProducts;
    private int productsWithSales;
    private int productsNeedingReorder;
    private int criticalCount;
    private int missingSupplierCount;
    private BigDecimal estimatedOrderCost;
    private String message;
    private List<ReorderSuggestionItemDTO> items;

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public int getLookbackDays() { return lookbackDays; }
    public void setLookbackDays(int lookbackDays) { this.lookbackDays = lookbackDays; }
    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
    public int getProductsWithSales() { return productsWithSales; }
    public void setProductsWithSales(int productsWithSales) { this.productsWithSales = productsWithSales; }
    public int getProductsNeedingReorder() { return productsNeedingReorder; }
    public void setProductsNeedingReorder(int productsNeedingReorder) { this.productsNeedingReorder = productsNeedingReorder; }
    public int getCriticalCount() { return criticalCount; }
    public void setCriticalCount(int criticalCount) { this.criticalCount = criticalCount; }
    public int getMissingSupplierCount() { return missingSupplierCount; }
    public void setMissingSupplierCount(int missingSupplierCount) { this.missingSupplierCount = missingSupplierCount; }
    public BigDecimal getEstimatedOrderCost() { return estimatedOrderCost; }
    public void setEstimatedOrderCost(BigDecimal estimatedOrderCost) { this.estimatedOrderCost = estimatedOrderCost; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<ReorderSuggestionItemDTO> getItems() { return items; }
    public void setItems(List<ReorderSuggestionItemDTO> items) { this.items = items; }
}
