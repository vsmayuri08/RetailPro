package com.retailsystem.dto;

import java.math.BigDecimal;

/**
 * One directed association A → B: among sales that contain product A,
 * how often they also contain product B. See MarketBasketService.
 */
public class ProductPairDTO {

    private String productAName;
    private String productBName;
    private int timesTogether;
    private BigDecimal supportPercent;
    private BigDecimal confidencePercent;

    public String getProductAName() { return productAName; }
    public void setProductAName(String productAName) { this.productAName = productAName; }
    public String getProductBName() { return productBName; }
    public void setProductBName(String productBName) { this.productBName = productBName; }
    public int getTimesTogether() { return timesTogether; }
    public void setTimesTogether(int timesTogether) { this.timesTogether = timesTogether; }
    public BigDecimal getSupportPercent() { return supportPercent; }
    public void setSupportPercent(BigDecimal supportPercent) { this.supportPercent = supportPercent; }
    public BigDecimal getConfidencePercent() { return confidencePercent; }
    public void setConfidencePercent(BigDecimal confidencePercent) { this.confidencePercent = confidencePercent; }
}
