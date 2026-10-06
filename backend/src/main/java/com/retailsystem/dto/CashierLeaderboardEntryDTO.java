package com.retailsystem.dto;

import java.math.BigDecimal;

public class CashierLeaderboardEntryDTO {

    private int rank;
    private Long cashierId;
    private String cashierName;
    private int salesCount;
    private int itemsSold;
    private BigDecimal revenue;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public Long getCashierId() { return cashierId; }
    public void setCashierId(Long cashierId) { this.cashierId = cashierId; }
    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }
    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }
    public int getItemsSold() { return itemsSold; }
    public void setItemsSold(int itemsSold) { this.itemsSold = itemsSold; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}
