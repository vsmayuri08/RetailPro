package com.retailsystem.dto;

import com.retailsystem.entity.SaleReturnItem;

import java.math.BigDecimal;

public class SaleReturnItemDTO {

    private Long id;
    private Long saleItemId;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineGoodsAmount;

    public static SaleReturnItemDTO fromEntity(SaleReturnItem item) {
        SaleReturnItemDTO dto = new SaleReturnItemDTO();
        dto.id = item.getId();
        dto.saleItemId = item.getOriginalItem().getId();
        dto.productId = item.getProduct().getId();
        dto.productName = item.getProductNameSnapshot();
        dto.quantity = item.getQuantity();
        dto.unitPrice = item.getUnitPrice();
        dto.lineGoodsAmount = item.getLineGoodsAmount();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSaleItemId() { return saleItemId; }
    public void setSaleItemId(Long saleItemId) { this.saleItemId = saleItemId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineGoodsAmount() { return lineGoodsAmount; }
    public void setLineGoodsAmount(BigDecimal lineGoodsAmount) { this.lineGoodsAmount = lineGoodsAmount; }
}
