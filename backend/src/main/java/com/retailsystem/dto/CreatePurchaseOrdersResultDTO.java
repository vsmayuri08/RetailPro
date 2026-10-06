package com.retailsystem.dto;

import java.util.ArrayList;
import java.util.List;

public class CreatePurchaseOrdersResultDTO {

    private List<PurchaseOrderDTO> orders = new ArrayList<>();
    private List<String> skippedNoSupplier = new ArrayList<>();
    private List<String> skippedOpenOrder = new ArrayList<>();
    private String notice;

    public List<PurchaseOrderDTO> getOrders() { return orders; }
    public void setOrders(List<PurchaseOrderDTO> orders) { this.orders = orders; }
    public List<String> getSkippedNoSupplier() { return skippedNoSupplier; }
    public void setSkippedNoSupplier(List<String> skippedNoSupplier) { this.skippedNoSupplier = skippedNoSupplier; }
    public List<String> getSkippedOpenOrder() { return skippedOpenOrder; }
    public void setSkippedOpenOrder(List<String> skippedOpenOrder) { this.skippedOpenOrder = skippedOpenOrder; }
    public String getNotice() { return notice; }
    public void setNotice(String notice) { this.notice = notice; }
}
