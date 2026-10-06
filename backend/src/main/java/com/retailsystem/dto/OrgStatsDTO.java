package com.retailsystem.dto;

import java.math.BigDecimal;
import java.util.List;

/** Organization-wide summary shown on the Super Admin dashboard and reports page. */
public class OrgStatsDTO {

    private long totalBranches;
    private long activeBranches;
    private long inactiveBranches;
    private long totalManagers;
    private long totalCashiers;
    private long totalEmployees;
    private List<BranchDTO> branches;

    // Module 12 -> org-wide sales figures, aggregated live from Sale records (never seeded/fake).
    private long totalSalesToday;
    private BigDecimal totalRevenueToday = BigDecimal.ZERO;
    private long totalSalesThisMonth;
    private BigDecimal totalRevenueThisMonth = BigDecimal.ZERO;
    private List<BranchSalesSummaryDTO> branchSales;

    public long getTotalBranches() { return totalBranches; }
    public void setTotalBranches(long totalBranches) { this.totalBranches = totalBranches; }
    public long getActiveBranches() { return activeBranches; }
    public void setActiveBranches(long activeBranches) { this.activeBranches = activeBranches; }
    public long getInactiveBranches() { return inactiveBranches; }
    public void setInactiveBranches(long inactiveBranches) { this.inactiveBranches = inactiveBranches; }
    public long getTotalManagers() { return totalManagers; }
    public void setTotalManagers(long totalManagers) { this.totalManagers = totalManagers; }
    public long getTotalCashiers() { return totalCashiers; }
    public void setTotalCashiers(long totalCashiers) { this.totalCashiers = totalCashiers; }
    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    public List<BranchDTO> getBranches() { return branches; }
    public void setBranches(List<BranchDTO> branches) { this.branches = branches; }
    public long getTotalSalesToday() { return totalSalesToday; }
    public void setTotalSalesToday(long totalSalesToday) { this.totalSalesToday = totalSalesToday; }
    public BigDecimal getTotalRevenueToday() { return totalRevenueToday; }
    public void setTotalRevenueToday(BigDecimal totalRevenueToday) { this.totalRevenueToday = totalRevenueToday; }
    public long getTotalSalesThisMonth() { return totalSalesThisMonth; }
    public void setTotalSalesThisMonth(long totalSalesThisMonth) { this.totalSalesThisMonth = totalSalesThisMonth; }
    public BigDecimal getTotalRevenueThisMonth() { return totalRevenueThisMonth; }
    public void setTotalRevenueThisMonth(BigDecimal totalRevenueThisMonth) { this.totalRevenueThisMonth = totalRevenueThisMonth; }
    public List<BranchSalesSummaryDTO> getBranchSales() { return branchSales; }
    public void setBranchSales(List<BranchSalesSummaryDTO> branchSales) { this.branchSales = branchSales; }
}
