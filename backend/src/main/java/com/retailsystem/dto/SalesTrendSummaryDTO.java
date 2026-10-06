package com.retailsystem.dto;

import java.math.BigDecimal;
import java.util.List;

public class SalesTrendSummaryDTO {

    private List<SalesTrendPointDTO> points;
    private BigDecimal predictedNextMonthRevenue;
    private BigDecimal expectedGrowthPercent;
    private BigDecimal lastCompleteMonthRevenue;
    private BigDecimal currentMonthToDateRevenue;
    private String method;
    private boolean enoughHistory;

    public List<SalesTrendPointDTO> getPoints() { return points; }
    public void setPoints(List<SalesTrendPointDTO> points) { this.points = points; }
    public BigDecimal getPredictedNextMonthRevenue() { return predictedNextMonthRevenue; }
    public void setPredictedNextMonthRevenue(BigDecimal predictedNextMonthRevenue) {
        this.predictedNextMonthRevenue = predictedNextMonthRevenue;
    }
    public BigDecimal getExpectedGrowthPercent() { return expectedGrowthPercent; }
    public void setExpectedGrowthPercent(BigDecimal expectedGrowthPercent) {
        this.expectedGrowthPercent = expectedGrowthPercent;
    }
    public BigDecimal getLastCompleteMonthRevenue() { return lastCompleteMonthRevenue; }
    public void setLastCompleteMonthRevenue(BigDecimal lastCompleteMonthRevenue) {
        this.lastCompleteMonthRevenue = lastCompleteMonthRevenue;
    }
    public BigDecimal getCurrentMonthToDateRevenue() { return currentMonthToDateRevenue; }
    public void setCurrentMonthToDateRevenue(BigDecimal currentMonthToDateRevenue) {
        this.currentMonthToDateRevenue = currentMonthToDateRevenue;
    }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public boolean isEnoughHistory() { return enoughHistory; }
    public void setEnoughHistory(boolean enoughHistory) { this.enoughHistory = enoughHistory; }
}
