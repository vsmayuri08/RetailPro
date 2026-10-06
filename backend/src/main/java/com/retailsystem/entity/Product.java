package com.retailsystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A product in a single branch's catalog. Also carries the inventory fields
 * (quantity/reorderLevel/expiryDate) directly rather than a separate
 * Inventory entity — a branch's stock is always 1:1 with its own product
 * row, so splitting it into another table would just add joins for no gain.
 * (quantity/expiryDate are themselves a synced cache of real InventoryBatch
 * data as of the Inventory Batches drop — see InventoryBatchService.)
 *
 * Module 3 completion -> barcode/brand/description/costPrice were added
 * here as optional fields (nullable, no behavior change to existing
 * consumers of this entity). "price" remains the SELLING price — the one
 * field Billing already reads everywhere — costPrice is new and separate,
 * for margin visibility.
 *
 * Module 3+4 completion -> category is now a real link to the Category
 * entity instead of free text, closing the gap flagged since the Category
 * Management drop. Nullable, since a product isn't required to have a
 * category assigned.
 */
@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_branch_sku", columnNames = {"branch_id", "sku"})
})
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sku;

    /** Optional alternate scannable identifier, distinct from sku. */
    private String barcode;

    private String brand;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Selling price — the figure Billing/discounts/receipts already use throughout. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Cost basis, optional — separate from the selling price above. */
    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false)
    private int quantity = 0;

    @Column(nullable = false)
    private int reorderLevel = 10;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    public Product() {
    }

    public Product(String name, String sku, Category category, BigDecimal price, int quantity,
                    int reorderLevel, LocalDate expiryDate, Branch branch) {
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
        this.branch = branch;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
}
