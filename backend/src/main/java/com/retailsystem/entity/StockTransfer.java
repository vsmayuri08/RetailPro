package com.retailsystem.entity;

import com.retailsystem.enums.StockTransferStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * A request from one branch manager to pull stock of an existing product
 * from another branch into their own branch. The product's branch is the
 * source (stock is deducted there); destinationBranch is where it lands.
 * Only the source branch's manager may approve/reject, since it's their
 * stock being depleted — this is what "approve where permitted" means in
 * the Branch Manager feature set.
 */
@Entity
@Table(name = "stock_transfers")
public class StockTransfer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_branch_id")
    private Branch destinationBranch;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockTransferStatus status = StockTransferStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    private LocalDateTime decidedAt;

    private String note;

    private String decisionNote;

    public StockTransfer() {
    }

    public StockTransfer(Product product, Branch destinationBranch, int quantity, User requestedBy, String note) {
        this.product = product;
        this.destinationBranch = destinationBranch;
        this.quantity = quantity;
        this.requestedBy = requestedBy;
        this.note = note;
        this.status = StockTransferStatus.PENDING;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Branch getDestinationBranch() { return destinationBranch; }
    public void setDestinationBranch(Branch destinationBranch) { this.destinationBranch = destinationBranch; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public StockTransferStatus getStatus() { return status; }
    public void setStatus(StockTransferStatus status) { this.status = status; }
    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }
    public User getDecidedBy() { return decidedBy; }
    public void setDecidedBy(User decidedBy) { this.decidedBy = decidedBy; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
