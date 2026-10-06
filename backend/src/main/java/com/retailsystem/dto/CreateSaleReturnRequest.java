package com.retailsystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreateSaleReturnRequest {

    @NotEmpty(message = "Return must include at least one item")
    @Valid
    private List<SaleReturnItemRequest> items;

    @NotBlank(message = "A reason is required")
    private String reason;

    public List<SaleReturnItemRequest> getItems() { return items; }
    public void setItems(List<SaleReturnItemRequest> items) { this.items = items; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
