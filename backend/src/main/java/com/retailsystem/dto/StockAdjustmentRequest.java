package com.retailsystem.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Quick inventory adjustment — delta can be positive (restock) or negative
 * (shrinkage/damage/correction). Kept separate from ProductRequest so the
 * "Manage inventory" action reads distinctly from "edit product details" in
 * the UI and the audit trail (reason).
 */
public class StockAdjustmentRequest {

    @NotNull(message = "Quantity change is required")
    private Integer delta;

    private String reason;

    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
