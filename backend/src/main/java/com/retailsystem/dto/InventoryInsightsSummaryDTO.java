package com.retailsystem.dto;

import java.util.List;

public class InventoryInsightsSummaryDTO {

    private String narrative;
    private int stockoutCount;
    private int overstockCount;
    private int deadStockCount;
    private int nearExpiryCount;
    private List<InventoryInsightItemDTO> items;

    public String getNarrative() { return narrative; }
    public void setNarrative(String narrative) { this.narrative = narrative; }
    public int getStockoutCount() { return stockoutCount; }
    public void setStockoutCount(int stockoutCount) { this.stockoutCount = stockoutCount; }
    public int getOverstockCount() { return overstockCount; }
    public void setOverstockCount(int overstockCount) { this.overstockCount = overstockCount; }
    public int getDeadStockCount() { return deadStockCount; }
    public void setDeadStockCount(int deadStockCount) { this.deadStockCount = deadStockCount; }
    public int getNearExpiryCount() { return nearExpiryCount; }
    public void setNearExpiryCount(int nearExpiryCount) { this.nearExpiryCount = nearExpiryCount; }
    public List<InventoryInsightItemDTO> getItems() { return items; }
    public void setItems(List<InventoryInsightItemDTO> items) { this.items = items; }
}
