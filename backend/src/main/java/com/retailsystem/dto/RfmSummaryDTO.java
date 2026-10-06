package com.retailsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RfmSummaryDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int totalCustomers;
    private BigDecimal totalMonetary;
    private String message;
    private List<RfmSegmentSummaryDTO> segmentSummaries;
    private List<RfmCustomerDTO> customers;

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }
    public BigDecimal getTotalMonetary() { return totalMonetary; }
    public void setTotalMonetary(BigDecimal totalMonetary) { this.totalMonetary = totalMonetary; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<RfmSegmentSummaryDTO> getSegmentSummaries() { return segmentSummaries; }
    public void setSegmentSummaries(List<RfmSegmentSummaryDTO> segmentSummaries) { this.segmentSummaries = segmentSummaries; }
    public List<RfmCustomerDTO> getCustomers() { return customers; }
    public void setCustomers(List<RfmCustomerDTO> customers) { this.customers = customers; }
}
