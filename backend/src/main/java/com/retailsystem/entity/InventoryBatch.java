package com.retailsystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Module 5 -> one batch of stock received for a product. This is now the
 * real source of truth for "how much of this product do we have and when
 * does each part of it expire" — {@link Product#getQuantity()} and
 * {@link Product#getExpiryDate()} are kept as a synced cache (see
 * InventoryBatchService#syncProductFromBatches) so every other part of the
 * app (billing, discounts, alerts) that already reads those two Product
 * fields keeps working unchanged, without needing to know batches exist.
 */
@Entity
@Table(name = "inventory_batches")
public class InventoryBatch extends BaseEntity {

    @Column(nullable = false)
    private String batchNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(nullable = false)
    private int quantityReceived;

    @Column(nullable = false)
    private int availableQuantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    private LocalDate expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private LocalDate receivedDate;

    public InventoryBatch() {
    }

    public InventoryBatch(String batchNumber, Product product, Branch branch, int quantityReceived,
                           BigDecimal purchasePrice, LocalDate expiryDate, Supplier supplier, LocalDate receivedDate) {
        this.batchNumber = batchNumber;
        this.product = product;
        this.branch = branch;
        this.quantityReceived = quantityReceived;
        this.availableQuantity = quantityReceived;
        this.purchasePrice = purchasePrice;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
        this.receivedDate = receivedDate;
    }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public int getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(int quantityReceived) { this.quantityReceived = quantityReceived; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
}
