package com.retailsystem.dto;

import java.math.BigDecimal;

/** Branch Manager dashboard summary — scoped to the manager's own branch. */
public class ManagerDashboardStatsDTO {

    private long totalProducts;
    private long lowStockCount;
    private long nearExpiryCount;
    private long totalEmployees;
    private long pendingIncomingTransfers;
    private long pendingOutgoingTransfers;
    private BigDecimal stockValue;

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }
    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }
    public long getNearExpiryCount() { return nearExpiryCount; }
    public void setNearExpiryCount(long nearExpiryCount) { this.nearExpiryCount = nearExpiryCount; }
    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    public long getPendingIncomingTransfers() { return pendingIncomingTransfers; }
    public void setPendingIncomingTransfers(long pendingIncomingTransfers) { this.pendingIncomingTransfers = pendingIncomingTransfers; }
    public long getPendingOutgoingTransfers() { return pendingOutgoingTransfers; }
    public void setPendingOutgoingTransfers(long pendingOutgoingTransfers) { this.pendingOutgoingTransfers = pendingOutgoingTransfers; }
    public BigDecimal getStockValue() { return stockValue; }
    public void setStockValue(BigDecimal stockValue) { this.stockValue = stockValue; }
}
