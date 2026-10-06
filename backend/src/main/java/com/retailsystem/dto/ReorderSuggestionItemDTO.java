package com.retailsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One product's reorder suggestion. Quantity and supplier come from sales
 * velocity and purchase-batch history — not from Product.reorderLevel.
 */
public class ReorderSuggestionItemDTO {

    private Long productId;
    private String productName;
    private String sku;
    private String category;
    private int currentStock;
    /** Catalog field shown only for contrast — not used to compute suggestedQty. */
    private int staticReorderLevel;
    private int unitsSold;
    private BigDecimal averageDailyUnitsSold;
    private int leadTimeDays;
    private int safetyDays;
    private int coverDays;
    private int targetStock;
    private int suggestedQty;
    private Integer estimatedDaysUntilStockout;
    private String urgency;
    private Long suggestedSupplierId;
    private String suggestedSupplierName;
    private String supplierReason;
    private LocalDate lastReceivedDate;
    private int purchaseBatchCount;
    private Integer typicalReceiptQty;
    private BigDecimal lastUnitCost;
    private BigDecimal estimatedOrderCost;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }
    public int getStaticReorderLevel() { return staticReorderLevel; }
    public void setStaticReorderLevel(int staticReorderLevel) { this.staticReorderLevel = staticReorderLevel; }
    public int getUnitsSold() { return unitsSold; }
    public void setUnitsSold(int unitsSold) { this.unitsSold = unitsSold; }
    public BigDecimal getAverageDailyUnitsSold() { return averageDailyUnitsSold; }
    public void setAverageDailyUnitsSold(BigDecimal averageDailyUnitsSold) { this.averageDailyUnitsSold = averageDailyUnitsSold; }
    public int getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(int leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public int getSafetyDays() { return safetyDays; }
    public void setSafetyDays(int safetyDays) { this.safetyDays = safetyDays; }
    public int getCoverDays() { return coverDays; }
    public void setCoverDays(int coverDays) { this.coverDays = coverDays; }
    public int getTargetStock() { return targetStock; }
    public void setTargetStock(int targetStock) { this.targetStock = targetStock; }
    public int getSuggestedQty() { return suggestedQty; }
    public void setSuggestedQty(int suggestedQty) { this.suggestedQty = suggestedQty; }
    public Integer getEstimatedDaysUntilStockout() { return estimatedDaysUntilStockout; }
    public void setEstimatedDaysUntilStockout(Integer estimatedDaysUntilStockout) { this.estimatedDaysUntilStockout = estimatedDaysUntilStockout; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public Long getSuggestedSupplierId() { return suggestedSupplierId; }
    public void setSuggestedSupplierId(Long suggestedSupplierId) { this.suggestedSupplierId = suggestedSupplierId; }
    public String getSuggestedSupplierName() { return suggestedSupplierName; }
    public void setSuggestedSupplierName(String suggestedSupplierName) { this.suggestedSupplierName = suggestedSupplierName; }
    public String getSupplierReason() { return supplierReason; }
    public void setSupplierReason(String supplierReason) { this.supplierReason = supplierReason; }
    public LocalDate getLastReceivedDate() { return lastReceivedDate; }
    public void setLastReceivedDate(LocalDate lastReceivedDate) { this.lastReceivedDate = lastReceivedDate; }
    public int getPurchaseBatchCount() { return purchaseBatchCount; }
    public void setPurchaseBatchCount(int purchaseBatchCount) { this.purchaseBatchCount = purchaseBatchCount; }
    public Integer getTypicalReceiptQty() { return typicalReceiptQty; }
    public void setTypicalReceiptQty(Integer typicalReceiptQty) { this.typicalReceiptQty = typicalReceiptQty; }
    public BigDecimal getLastUnitCost() { return lastUnitCost; }
    public void setLastUnitCost(BigDecimal lastUnitCost) { this.lastUnitCost = lastUnitCost; }
    public BigDecimal getEstimatedOrderCost() { return estimatedOrderCost; }
    public void setEstimatedOrderCost(BigDecimal estimatedOrderCost) { this.estimatedOrderCost = estimatedOrderCost; }
}
