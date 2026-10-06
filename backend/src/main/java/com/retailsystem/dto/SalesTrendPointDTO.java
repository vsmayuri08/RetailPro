package com.retailsystem.dto;

import java.math.BigDecimal;

public class SalesTrendPointDTO {

    private String yearMonth;
    private String label;
    private BigDecimal revenue;
    /** HISTORICAL, CURRENT (month-to-date), or PREDICTED */
    private String kind;

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
}
