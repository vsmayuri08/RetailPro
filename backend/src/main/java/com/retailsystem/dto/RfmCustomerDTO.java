package com.retailsystem.dto;

import java.math.BigDecimal;

/**
 * One customer's RFM row. Scores are 1–5 (5 is best). Segment is derived
 * from those scores — see RfmAnalysisService.
 */
public class RfmCustomerDTO {

    private Long customerId;
    private String customerName;
    private String phone;
    private String loyaltyTier;
    private int recencyDays;
    private int frequency;
    private BigDecimal monetary;
    private int recencyScore;
    private int frequencyScore;
    private int monetaryScore;
    private String rfmCode;
    private String segment;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLoyaltyTier() { return loyaltyTier; }
    public void setLoyaltyTier(String loyaltyTier) { this.loyaltyTier = loyaltyTier; }
    public int getRecencyDays() { return recencyDays; }
    public void setRecencyDays(int recencyDays) { this.recencyDays = recencyDays; }
    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
    public BigDecimal getMonetary() { return monetary; }
    public void setMonetary(BigDecimal monetary) { this.monetary = monetary; }
    public int getRecencyScore() { return recencyScore; }
    public void setRecencyScore(int recencyScore) { this.recencyScore = recencyScore; }
    public int getFrequencyScore() { return frequencyScore; }
    public void setFrequencyScore(int frequencyScore) { this.frequencyScore = frequencyScore; }
    public int getMonetaryScore() { return monetaryScore; }
    public void setMonetaryScore(int monetaryScore) { this.monetaryScore = monetaryScore; }
    public String getRfmCode() { return rfmCode; }
    public void setRfmCode(String rfmCode) { this.rfmCode = rfmCode; }
    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }
}
