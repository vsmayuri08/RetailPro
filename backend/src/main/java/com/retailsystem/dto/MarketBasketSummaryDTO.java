package com.retailsystem.dto;

import java.time.LocalDate;
import java.util.List;

public class MarketBasketSummaryDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int totalTransactions;
    private int minSupport;
    private String message;
    private List<ProductPairDTO> pairs;

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public int getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
    public int getMinSupport() { return minSupport; }
    public void setMinSupport(int minSupport) { this.minSupport = minSupport; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<ProductPairDTO> getPairs() { return pairs; }
    public void setPairs(List<ProductPairDTO> pairs) { this.pairs = pairs; }
}
