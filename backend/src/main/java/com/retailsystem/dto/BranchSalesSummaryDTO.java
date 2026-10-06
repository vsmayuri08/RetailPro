package com.retailsystem.dto;

import java.math.BigDecimal;

/** One branch's row in the Super Admin's branch performance comparison. */
public class BranchSalesSummaryDTO {

    private Long branchId;
    private String branchName;
    private long salesCountToday;
    private BigDecimal revenueToday;
    private long salesCountThisMonth;
    private BigDecimal revenueThisMonth;

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public long getSalesCountToday() { return salesCountToday; }
    public void setSalesCountToday(long salesCountToday) { this.salesCountToday = salesCountToday; }
    public BigDecimal getRevenueToday() { return revenueToday; }
    public void setRevenueToday(BigDecimal revenueToday) { this.revenueToday = revenueToday; }
    public long getSalesCountThisMonth() { return salesCountThisMonth; }
    public void setSalesCountThisMonth(long salesCountThisMonth) { this.salesCountThisMonth = salesCountThisMonth; }
    public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
    public void setRevenueThisMonth(BigDecimal revenueThisMonth) { this.revenueThisMonth = revenueThisMonth; }
}
