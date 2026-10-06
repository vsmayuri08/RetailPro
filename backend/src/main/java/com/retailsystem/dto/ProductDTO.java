package com.retailsystem.dto;

import com.retailsystem.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductDTO {

    /** Products expiring within this many days are flagged "near expiry". */
    public static final int NEAR_EXPIRY_DAYS = 14;

    private Long id;
    private String name;
    private String sku;
    private String barcode;
    private String brand;
    private String description;
    /** Category name for display — kept as a plain string so every existing screen that already
     *  renders "product.category" keeps working unchanged now that Product links to a real Category. */
    private String category;
    private Long categoryId;
    private BigDecimal price;
    private BigDecimal costPrice;
    private int quantity;
    private int reorderLevel;
    private LocalDate expiryDate;
    private boolean active;
    private Long branchId;
    private String branchName;
    private boolean lowStock;
    private boolean nearExpiry;
    private boolean expired;
    /** Module 10 -> the discount currently in effect, per the Super Admin's rule ladder. 0 if none apply. */
    private BigDecimal expiryDiscountPercent = BigDecimal.ZERO;
    /** price with expiryDiscountPercent applied — equals price when there's no active discount. */
    private BigDecimal discountedPrice;
    private LocalDateTime createdAt;

    public static ProductDTO fromEntity(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.sku = product.getSku();
        dto.barcode = product.getBarcode();
        dto.brand = product.getBrand();
        dto.description = product.getDescription();
        dto.category = product.getCategory() != null ? product.getCategory().getName() : null;
        dto.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        dto.price = product.getPrice();
        dto.costPrice = product.getCostPrice();
        dto.quantity = product.getQuantity();
        dto.reorderLevel = product.getReorderLevel();
        dto.expiryDate = product.getExpiryDate();
        dto.active = product.isActive();
        dto.branchId = product.getBranch() != null ? product.getBranch().getId() : null;
        dto.branchName = product.getBranch() != null ? product.getBranch().getBranchName() : null;
        dto.lowStock = product.getQuantity() <= product.getReorderLevel();
        LocalDate today = LocalDate.now();
        dto.expired = product.getExpiryDate() != null && product.getExpiryDate().isBefore(today);
        dto.nearExpiry = product.getExpiryDate() != null && !dto.expired
                && !product.getExpiryDate().isAfter(today.plusDays(NEAR_EXPIRY_DAYS));
        dto.discountedPrice = dto.price; // overwritten by ProductService once the expiry discount rate is looked up
        dto.createdAt = product.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
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
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public boolean isLowStock() { return lowStock; }
    public void setLowStock(boolean lowStock) { this.lowStock = lowStock; }
    public boolean isNearExpiry() { return nearExpiry; }
    public void setNearExpiry(boolean nearExpiry) { this.nearExpiry = nearExpiry; }
    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
    public BigDecimal getExpiryDiscountPercent() { return expiryDiscountPercent; }
    public void setExpiryDiscountPercent(BigDecimal expiryDiscountPercent) { this.expiryDiscountPercent = expiryDiscountPercent; }
    public BigDecimal getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
