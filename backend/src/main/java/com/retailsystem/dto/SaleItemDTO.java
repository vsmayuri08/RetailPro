package com.retailsystem.dto;

import com.retailsystem.entity.SaleItem;

import java.math.BigDecimal;

public class SaleItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private int quantity;
    private BigDecimal originalUnitPrice;
    private BigDecimal expiryDiscountPercent;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private int returnedQuantity;

    public static SaleItemDTO fromEntity(SaleItem item) {
        SaleItemDTO dto = new SaleItemDTO();
        dto.id = item.getId();
        dto.productId = item.getProduct().getId();
        dto.productName = item.getProductNameSnapshot();
        dto.productSku = item.getProductSkuSnapshot();
        dto.quantity = item.getQuantity();
        dto.originalUnitPrice = item.getOriginalUnitPrice();
        dto.expiryDiscountPercent = item.getExpiryDiscountPercent();
        dto.unitPrice = item.getUnitPrice();
        dto.lineTotal = item.getLineTotal();
        dto.returnedQuantity = 0;
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getOriginalUnitPrice() { return originalUnitPrice; }
    public void setOriginalUnitPrice(BigDecimal originalUnitPrice) { this.originalUnitPrice = originalUnitPrice; }
    public BigDecimal getExpiryDiscountPercent() { return expiryDiscountPercent; }
    public void setExpiryDiscountPercent(BigDecimal expiryDiscountPercent) { this.expiryDiscountPercent = expiryDiscountPercent; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(int returnedQuantity) { this.returnedQuantity = returnedQuantity; }
}
