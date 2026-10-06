package com.retailsystem.dto;

import com.retailsystem.entity.StockTransfer;

import java.time.LocalDateTime;

public class StockTransferDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private int quantity;
    private String status;

    private Long sourceBranchId;
    private String sourceBranchName;
    private Long destinationBranchId;
    private String destinationBranchName;

    private String requestedByName;
    private LocalDateTime requestedAt;
    private String decidedByName;
    private LocalDateTime decidedAt;
    private String note;
    private String decisionNote;

    public static StockTransferDTO fromEntity(StockTransfer transfer) {
        StockTransferDTO dto = new StockTransferDTO();
        dto.id = transfer.getId();
        dto.productId = transfer.getProduct().getId();
        dto.productName = transfer.getProduct().getName();
        dto.productSku = transfer.getProduct().getSku();
        dto.quantity = transfer.getQuantity();
        dto.status = transfer.getStatus().name();

        dto.sourceBranchId = transfer.getProduct().getBranch().getId();
        dto.sourceBranchName = transfer.getProduct().getBranch().getBranchName();
        dto.destinationBranchId = transfer.getDestinationBranch().getId();
        dto.destinationBranchName = transfer.getDestinationBranch().getBranchName();

        dto.requestedByName = transfer.getRequestedBy().getFullName();
        dto.requestedAt = transfer.getCreatedAt();
        dto.decidedByName = transfer.getDecidedBy() != null ? transfer.getDecidedBy().getFullName() : null;
        dto.decidedAt = transfer.getDecidedAt();
        dto.note = transfer.getNote();
        dto.decisionNote = transfer.getDecisionNote();
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getSourceBranchId() { return sourceBranchId; }
    public void setSourceBranchId(Long sourceBranchId) { this.sourceBranchId = sourceBranchId; }
    public String getSourceBranchName() { return sourceBranchName; }
    public void setSourceBranchName(String sourceBranchName) { this.sourceBranchName = sourceBranchName; }
    public Long getDestinationBranchId() { return destinationBranchId; }
    public void setDestinationBranchId(Long destinationBranchId) { this.destinationBranchId = destinationBranchId; }
    public String getDestinationBranchName() { return destinationBranchName; }
    public void setDestinationBranchName(String destinationBranchName) { this.destinationBranchName = destinationBranchName; }
    public String getRequestedByName() { return requestedByName; }
    public void setRequestedByName(String requestedByName) { this.requestedByName = requestedByName; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public String getDecidedByName() { return decidedByName; }
    public void setDecidedByName(String decidedByName) { this.decidedByName = decidedByName; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
