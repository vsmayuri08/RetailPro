package com.retailsystem.service;

import com.retailsystem.dto.StockTransferDTO;
import com.retailsystem.dto.StockTransferRequest;
import com.retailsystem.dto.TransferDecisionRequest;
import com.retailsystem.entity.Branch;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.StockTransfer;
import com.retailsystem.entity.User;
import com.retailsystem.enums.StockTransferStatus;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.StockTransferRepository;
import com.retailsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.retailsystem.exception.UnauthorizedBranchAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Branch Manager -> "Approve stock transfers where permitted".
 *
 * A manager requests stock FROM another branch's catalog INTO their own
 * branch. The request can only be approved or rejected by the manager of
 * the SOURCE branch (the one whose stock would be deducted) — that's the
 * "where permitted" restriction. On approval the source product's quantity
 * is reduced and a matching (by SKU) product in the destination branch is
 * created or topped up.
 */
@Service
public class StockTransferService {

    @Autowired
    private StockTransferRepository stockTransferRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Transactional
    public StockTransferDTO createRequest(Long destinationBranchId, Long requestedByUserId, StockTransferRequest request) {
        Product sourceProduct = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (sourceProduct.getBranch().getId().equals(destinationBranchId)) {
            throw new BadRequestException("Choose a product from a different branch to request a transfer");
        }
        if (!sourceProduct.isActive()) {
            throw new BadRequestException("This product is not active at the source branch");
        }

        Branch destinationBranch = branchRepository.findById(destinationBranchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", destinationBranchId));
        User requestedBy = userRepository.findById(requestedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requestedByUserId));

        StockTransfer transfer = new StockTransfer(sourceProduct, destinationBranch, request.getQuantity(), requestedBy, request.getNote());
        transfer = stockTransferRepository.save(transfer);
        return StockTransferDTO.fromEntity(transfer);
    }

    /** Requests awaiting THIS branch's decision, because this branch is the source. */
    public List<StockTransferDTO> getIncoming(Long branchId) {
        return stockTransferRepository.findByProduct_Branch_IdOrderByCreatedAtDesc(branchId).stream()
                .map(StockTransferDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Requests THIS branch raised, as the destination. */
    public List<StockTransferDTO> getOutgoing(Long branchId) {
        return stockTransferRepository.findByDestinationBranch_IdOrderByCreatedAtDesc(branchId).stream()
                .map(StockTransferDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockTransferDTO decide(Long actingBranchId, Long actingUserId, Long transferId, boolean approve, TransferDecisionRequest request) {
        StockTransfer transfer = stockTransferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transfer", "id", transferId));

        if (!transfer.getProduct().getBranch().getId().equals(actingBranchId)) {
            throw new UnauthorizedBranchAccessException("Only the source branch's manager can decide this transfer");
        }
        if (transfer.getStatus() != StockTransferStatus.PENDING) {
            throw new BadRequestException("This transfer has already been " + transfer.getStatus().name().toLowerCase());
        }

        User decidedBy = userRepository.findById(actingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", actingUserId));

        if (approve) {
            Product sourceProduct = transfer.getProduct();
            if (sourceProduct.getQuantity() < transfer.getQuantity()) {
                throw new BadRequestException("Insufficient stock to approve this transfer (available: " + sourceProduct.getQuantity() + ")");
            }
            // Capture the expiry BEFORE deducting — deductFifo resyncs sourceProduct's expiry to
            // whatever's left behind afterward, which is not what's actually being shipped out.
            // Using the pre-deduction value (the batch FIFO is about to draw from first) as the
            // transferred stock's expiry is a reasonable approximation; it's only imprecise if a
            // single transfer spans multiple source batches with different expiry dates.
            LocalDate transferredExpiry = sourceProduct.getExpiryDate();

            // Module 5 -> FIFO-deduct from the source branch's real batches (keeps
            // Product.quantity/expiryDate in sync there), then land the transferred stock at the
            // destination as its own real batch, not just a quantity bump with nothing behind it.
            inventoryBatchService.deductFifo(sourceProduct, transfer.getQuantity());
            upsertDestinationStock(sourceProduct, transfer.getDestinationBranch(), transfer.getQuantity(), transferredExpiry);
            transfer.setStatus(StockTransferStatus.APPROVED);
        } else {
            transfer.setStatus(StockTransferStatus.REJECTED);
        }

        transfer.setDecidedBy(decidedBy);
        transfer.setDecidedAt(LocalDateTime.now());
        if (request != null) {
            transfer.setDecisionNote(request.getDecisionNote());
        }
        transfer = stockTransferRepository.save(transfer);
        return StockTransferDTO.fromEntity(transfer);
    }

    private void upsertDestinationStock(Product sourceProduct, Branch destinationBranch, int quantity, LocalDate transferredExpiry) {
        Product destinationProduct = productRepository
                .findByBranchIdAndSkuIgnoreCase(destinationBranch.getId(), sourceProduct.getSku())
                .orElse(null);

        if (destinationProduct == null) {
            destinationProduct = new Product(
                    sourceProduct.getName(),
                    sourceProduct.getSku(),
                    sourceProduct.getCategory(),
                    sourceProduct.getPrice(),
                    0, // starts at 0 — receiveTransferredStock below adds the real batch + quantity
                    sourceProduct.getReorderLevel(),
                    null,
                    destinationBranch
            );
            destinationProduct = productRepository.save(destinationProduct);
        }

        // Module 5 -> lands as a real batch at the destination (sellable via FIFO there too),
        // not just a quantity bump with nothing behind it.
        inventoryBatchService.receiveTransferredStock(destinationProduct, sourceProduct.getBranch().getBranchCode(), quantity, transferredExpiry);
    }
}
