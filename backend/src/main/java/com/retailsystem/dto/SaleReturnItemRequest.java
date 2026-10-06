package com.retailsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SaleReturnItemRequest {

    @NotNull(message = "Sale line is required")
    private Long saleItemId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public Long getSaleItemId() { return saleItemId; }
    public void setSaleItemId(Long saleItemId) { this.saleItemId = saleItemId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
