package com.retailsystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * One line item on a Sale. Product name/SKU are snapshotted at sale time
 * (in addition to the FK) so a receipt still reads correctly even if the
 * product is later renamed or deactivated.
 *
 * Module 10 -> originalUnitPrice is the product's catalog price at the
 * moment of sale; unitPrice is what was actually charged after applying
 * that moment's expiry-based discount (they're equal when no discount
 * applied). lineTotal is always quantity * unitPrice — the discounted
 * figure — since that's what the customer actually paid.
 */
@Entity
@Table(name = "sale_items")
public class SaleItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String productNameSnapshot;

    @Column(nullable = false)
    private String productSkuSnapshot;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalUnitPrice;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal expiryDiscountPercent = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    public SaleItem() {
    }

    public SaleItem(Sale sale, Product product, int quantity, BigDecimal originalUnitPrice,
                     BigDecimal expiryDiscountPercent, BigDecimal effectiveUnitPrice) {
        this.sale = sale;
        this.product = product;
        this.productNameSnapshot = product.getName();
        this.productSkuSnapshot = product.getSku();
        this.quantity = quantity;
        this.originalUnitPrice = originalUnitPrice;
        this.expiryDiscountPercent = expiryDiscountPercent;
        this.unitPrice = effectiveUnitPrice;
        this.lineTotal = effectiveUnitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String productNameSnapshot) { this.productNameSnapshot = productNameSnapshot; }
    public String getProductSkuSnapshot() { return productSkuSnapshot; }
    public void setProductSkuSnapshot(String productSkuSnapshot) { this.productSkuSnapshot = productSkuSnapshot; }
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
}
