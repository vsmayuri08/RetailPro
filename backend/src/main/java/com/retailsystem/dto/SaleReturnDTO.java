package com.retailsystem.dto;

import com.retailsystem.entity.SaleReturn;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SaleReturnDTO {

    private Long id;
    private Long saleId;
    private String invoiceNumber;
    private String processedByName;
    private String reason;
    private BigDecimal refundAmount;
    private int loyaltyPointsReversed;
    private List<SaleReturnItemDTO> items;
    private LocalDateTime createdAt;

    public static SaleReturnDTO fromEntity(SaleReturn saleReturn) {
        SaleReturnDTO dto = new SaleReturnDTO();
        dto.id = saleReturn.getId();
        dto.saleId = saleReturn.getSale().getId();
        dto.invoiceNumber = saleReturn.getSale().getInvoiceNumber();
        dto.processedByName = saleReturn.getProcessedBy().getFullName();
        dto.reason = saleReturn.getReason();
        dto.refundAmount = saleReturn.getRefundAmount();
        dto.loyaltyPointsReversed = saleReturn.getLoyaltyPointsReversed();
        dto.items = saleReturn.getItems().stream().map(SaleReturnItemDTO::fromEntity).collect(Collectors.toList());
        dto.createdAt = saleReturn.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSaleId() { return saleId; }
    public void setSaleId(Long saleId) { this.saleId = saleId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getProcessedByName() { return processedByName; }
    public void setProcessedByName(String processedByName) { this.processedByName = processedByName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public int getLoyaltyPointsReversed() { return loyaltyPointsReversed; }
    public void setLoyaltyPointsReversed(int loyaltyPointsReversed) { this.loyaltyPointsReversed = loyaltyPointsReversed; }
    public List<SaleReturnItemDTO> getItems() { return items; }
    public void setItems(List<SaleReturnItemDTO> items) { this.items = items; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
