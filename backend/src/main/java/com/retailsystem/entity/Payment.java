package com.retailsystem.entity;

import com.retailsystem.enums.PaymentMethod;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * How a Sale was paid for. One-to-one with Sale in this drop (no split
 * payments yet) — cashReceived/changeAmount are only meaningful for CASH;
 * for CARD/UPI they mirror the total (no change to give).
 */
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cashReceived;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal changeAmount;

    public Payment() {
    }

    public Payment(Sale sale, PaymentMethod method, BigDecimal cashReceived, BigDecimal changeAmount) {
        this.sale = sale;
        this.method = method;
        this.cashReceived = cashReceived;
        this.changeAmount = changeAmount;
    }

    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal cashReceived) { this.cashReceived = cashReceived; }
    public BigDecimal getChangeAmount() { return changeAmount; }
    public void setChangeAmount(BigDecimal changeAmount) { this.changeAmount = changeAmount; }
}
