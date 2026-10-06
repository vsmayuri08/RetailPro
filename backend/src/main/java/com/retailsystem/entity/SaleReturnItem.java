package com.retailsystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * One line on a SaleReturn — quantity coming back against a specific
 * original SaleItem, at the unit price the customer actually paid.
 */
@Entity
@Table(name = "sale_return_items")
public class SaleReturnItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_return_id")
    private SaleReturn saleReturn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_item_id")
    private SaleItem originalItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String productNameSnapshot;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineGoodsAmount;

    public SaleReturnItem() {
    }

    public SaleReturnItem(SaleReturn saleReturn, SaleItem originalItem, int quantity) {
        this.saleReturn = saleReturn;
        this.originalItem = originalItem;
        this.product = originalItem.getProduct();
        this.productNameSnapshot = originalItem.getProductNameSnapshot();
        this.quantity = quantity;
        this.unitPrice = originalItem.getUnitPrice();
        this.lineGoodsAmount = originalItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public SaleReturn getSaleReturn() { return saleReturn; }
    public void setSaleReturn(SaleReturn saleReturn) { this.saleReturn = saleReturn; }
    public SaleItem getOriginalItem() { return originalItem; }
    public void setOriginalItem(SaleItem originalItem) { this.originalItem = originalItem; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineGoodsAmount() { return lineGoodsAmount; }
    public void setLineGoodsAmount(BigDecimal lineGoodsAmount) { this.lineGoodsAmount = lineGoodsAmount; }
}
