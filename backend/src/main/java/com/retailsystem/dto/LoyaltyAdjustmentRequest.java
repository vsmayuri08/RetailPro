package com.retailsystem.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Manually add/deduct loyalty points (e.g. goodwill adjustment, or a
 * correction). Once the Billing module exists, a completed sale will call
 * the same CustomerService method this endpoint calls, so the tier
 * recalculation logic only needs to live in one place.
 */
public class LoyaltyAdjustmentRequest {

    @NotNull(message = "Points delta is required")
    private Integer delta;

    private String reason;

    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
