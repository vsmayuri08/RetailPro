package com.retailsystem.dto;

import com.retailsystem.entity.Payment;

import java.math.BigDecimal;

public class PaymentDTO {

    private String method;
    private BigDecimal cashReceived;
    private BigDecimal changeAmount;

    public static PaymentDTO fromEntity(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.method = payment.getMethod().name();
        dto.cashReceived = payment.getCashReceived();
        dto.changeAmount = payment.getChangeAmount();
        return dto;
    }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal cashReceived) { this.cashReceived = cashReceived; }
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
}
