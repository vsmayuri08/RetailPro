package com.retailsystem.dto;

import java.math.BigDecimal;

public class RfmSegmentSummaryDTO {

    private String segment;
    private int customerCount;
    private BigDecimal customerCountPercent;
    private BigDecimal totalMonetary;

    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }
    public int getCustomerCount() { return customerCount; }
    public void setCustomerCount(int customerCount) { this.customerCount = customerCount; }
    public BigDecimal getCustomerCountPercent() { return customerCountPercent; }
    public void setCustomerCountPercent(BigDecimal customerCountPercent) { this.customerCountPercent = customerCountPercent; }
    public BigDecimal getTotalMonetary() { return totalMonetary; }
    public void setTotalMonetary(BigDecimal totalMonetary) { this.totalMonetary = totalMonetary; }
}
