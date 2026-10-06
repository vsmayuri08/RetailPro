package com.retailsystem.dto;

import com.retailsystem.entity.InventoryBatch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryBatchDTO {

    private Long id;
    private String batchNumber;
    private Long productId;
    private String productName;
    private int quantityReceived;
    private int availableQuantity;
    private BigDecimal purchasePrice;
    private LocalDate expiryDate;
    private Long supplierId;
    private String supplierName;
    private LocalDate receivedDate;
    private LocalDateTime createdAt;

    public static InventoryBatchDTO fromEntity(InventoryBatch batch) {
        InventoryBatchDTO dto = new InventoryBatchDTO();
        dto.id = batch.getId();
        dto.batchNumber = batch.getBatchNumber();
        dto.productId = batch.getProduct().getId();
        dto.productName = batch.getProduct().getName();
        dto.quantityReceived = batch.getQuantityReceived();
        dto.availableQuantity = batch.getAvailableQuantity();
        dto.purchasePrice = batch.getPurchasePrice();
        dto.expiryDate = batch.getExpiryDate();
        dto.supplierId = batch.getSupplier() != null ? batch.getSupplier().getId() : null;
        dto.supplierName = batch.getSupplier() != null ? batch.getSupplier().getName() : null;
        dto.receivedDate = batch.getReceivedDate();
        dto.createdAt = batch.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(int quantityReceived) { this.quantityReceived = quantityReceived; }
    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
