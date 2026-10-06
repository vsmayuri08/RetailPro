package com.retailsystem.dto;

import com.retailsystem.entity.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SaleDTO {

    private Long id;
    private String invoiceNumber;
    private Long branchId;
    private String branchName;
    private Long customerId;
    private String customerName;
    private String processedByName;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private int loyaltyPointsEarned;
    private List<SaleItemDTO> items;
    private PaymentDTO payment;
    private LocalDateTime createdAt;
    private BigDecimal refundedAmount;
    private String receiptToken;

    public static SaleDTO fromEntity(Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.id = sale.getId();
        dto.invoiceNumber = sale.getInvoiceNumber();
        dto.branchId = sale.getBranch().getId();
        dto.branchName = sale.getBranch().getBranchName();
        dto.customerId = sale.getCustomer() != null ? sale.getCustomer().getId() : null;
        dto.customerName = sale.getCustomer() != null ? sale.getCustomer().getFullName() : null;
        dto.processedByName = sale.getProcessedBy().getFullName();
        dto.subtotal = sale.getSubtotal();
        dto.discountAmount = sale.getDiscountAmount();
        dto.taxAmount = sale.getTaxAmount();
        dto.totalAmount = sale.getTotalAmount();
        dto.loyaltyPointsEarned = sale.getLoyaltyPointsEarned();
        dto.items = sale.getItems().stream().map(SaleItemDTO::fromEntity).collect(Collectors.toList());
        dto.payment = sale.getPayment() != null ? PaymentDTO.fromEntity(sale.getPayment()) : null;
        dto.createdAt = sale.getCreatedAt();
        dto.refundedAmount = BigDecimal.ZERO;
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getProcessedByName() { return processedByName; }
    public void setProcessedByName(String processedByName) { this.processedByName = processedByName; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public int getLoyaltyPointsEarned() { return loyaltyPointsEarned; }
    public void setLoyaltyPointsEarned(int loyaltyPointsEarned) { this.loyaltyPointsEarned = loyaltyPointsEarned; }
    public List<SaleItemDTO> getItems() { return items; }
    public void setItems(List<SaleItemDTO> items) { this.items = items; }
    public PaymentDTO getPayment() { return payment; }
    public void setPayment(PaymentDTO payment) { this.payment = payment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(BigDecimal refundedAmount) { this.refundedAmount = refundedAmount; }
    public String getReceiptToken() { return receiptToken; }
    public void setReceiptToken(String receiptToken) { this.receiptToken = receiptToken; }
}
