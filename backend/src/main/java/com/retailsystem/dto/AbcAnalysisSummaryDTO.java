package com.retailsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AbcAnalysisSummaryDTO {

    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal totalRevenue;
    private int totalProducts;
    private List<AbcClassSummaryDTO> classSummaries;
    private List<AbcAnalysisItemDTO> items;

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public int getTotalProducts() { return totalProducts; }
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
    public List<AbcClassSummaryDTO> getClassSummaries() { return classSummaries; }
    public void setClassSummaries(List<AbcClassSummaryDTO> classSummaries) { this.classSummaries = classSummaries; }
    public List<AbcAnalysisItemDTO> getItems() { return items; }
    public void setItems(List<AbcAnalysisItemDTO> items) { this.items = items; }
}
