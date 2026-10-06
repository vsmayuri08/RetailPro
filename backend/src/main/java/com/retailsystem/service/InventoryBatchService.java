package com.retailsystem.service;

import com.retailsystem.dto.InventoryBatchDTO;
import com.retailsystem.dto.ReceiveStockRequest;
import com.retailsystem.entity.InventoryBatch;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.Supplier;
import com.retailsystem.exception.InsufficientStockException;
import com.retailsystem.exception.ProductNotFoundException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.InventoryBatchRepository;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 5 -> real batch-level inventory, sitting underneath the simpler
 * Product.quantity/expiryDate fields everything else in the app already
 * reads. This service is the only thing that touches InventoryBatch rows
 * directly; ProductService (on create), SaleService (on checkout),
 * StockTransferService (on an approved transfer), and SaleReturnService
 * (on a processed return) all call into it rather than manipulating
 * batches — or Product.quantity — themselves, so the
 * "keep Product's cached fields in sync with real batch data" rule only
 * has to be enforced in one place.
 *
 * Deliberately depends only on repositories, not on ProductService, so
 * ProductService can depend on this service (for opening batches) without
 * creating a circular bean dependency.
 */
@Service
public class InventoryBatchService {

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    /** Called once, right after a new Product is created with an opening quantity > 0. */
    @Transactional
    public void createOpeningBatch(Product product, int quantity, LocalDate expiryDate) {
        if (quantity <= 0) {
            return;
        }
        // Product.quantity/expiryDate were already set directly by ProductService at creation
        // time to this same value, so this only needs to create the batch record itself, not
        // touch the product again (addBatch's product-side effects would double-count it).
        InventoryBatch batch = new InventoryBatch(
                "OPENING-" + product.getSku(), product, product.getBranch(),
                quantity, null, expiryDate, null, LocalDate.now());
        inventoryBatchRepository.save(batch);
    }

    /** Module 5 -> "Add stock" / "Record stock received", from the Branch Manager's Receive Stock form. */
    @Transactional
    public InventoryBatchDTO receiveStock(Long branchId, Long productId, ReceiveStockRequest request) {
        Product product = getOwnedProductOrThrow(branchId, productId);

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
        }

        String batchNumber = (request.getBatchNumber() == null || request.getBatchNumber().isBlank())
                ? "BATCH-" + product.getSku() + "-" + System.currentTimeMillis()
                : request.getBatchNumber();
        LocalDate receivedDate = request.getReceivedDate() != null ? request.getReceivedDate() : LocalDate.now();

        InventoryBatch batch = addBatch(product, batchNumber, request.getQuantityReceived(),
                request.getPurchasePrice(), request.getExpiryDate(), supplier, receivedDate);
        return InventoryBatchDTO.fromEntity(batch);
    }

    /**
     * Module 11 -> called by StockTransferService when an approved transfer lands at the
     * destination branch's matching product, so the incoming stock is real batch data (and
     * therefore sellable via FIFO) rather than just a quantity bump with nothing behind it.
     */
    @Transactional
    public void receiveTransferredStock(Product destinationProduct, String sourceBranchCode, int quantity, LocalDate expiryDate) {
        String batchNumber = "TRANSFER-" + sourceBranchCode + "-" + System.currentTimeMillis();
        addBatch(destinationProduct, batchNumber, quantity, null, expiryDate, null, LocalDate.now());
    }

    /**
     * Restock goods coming back from a sale return. Adds a RETURN- batch so
     * the quantity is real FIFO inventory again, and updates Product.quantity
     * here — callers must not touch Product.quantity themselves.
     */
    @Transactional
    public void restockReturned(Product product, int quantity, LocalDate expiryDate) {
        if (quantity <= 0) {
            return;
        }
        String batchNumber = "RETURN-" + product.getSku() + "-" + System.currentTimeMillis();
        addBatch(product, batchNumber, quantity, null, expiryDate, null, LocalDate.now());
    }

    /**
     * Module 5 -> FIFO stock deduction, called by SaleService at checkout and by
     * StockTransferService when an approved transfer depletes the source branch's stock —
     * instead of either decrementing Product.quantity directly. Walks batches
     * earliest-expiry-first until quantityToDeduct is satisfied. Callers are expected to have
     * already checked product.quantity >= quantityToDeduct for a fast, clear error message;
     * product.quantity is kept exactly equal to the sum of batch availableQuantity by every
     * method in this class, so that check remains reliable, and this method still re-validates
     * at the batch level as a safety net.
     */
    @Transactional
    public void deductFifo(Product product, int quantityToDeduct) {
        List<InventoryBatch> batches = inventoryBatchRepository.findAvailableFifoOrder(product.getId());
        int remaining = quantityToDeduct;

        for (InventoryBatch batch : batches) {
            if (remaining <= 0) {
                break;
            }
            int takeFromBatch = Math.min(remaining, batch.getAvailableQuantity());
            batch.setAvailableQuantity(batch.getAvailableQuantity() - takeFromBatch);
            inventoryBatchRepository.save(batch);
            remaining -= takeFromBatch;
        }

        if (remaining > 0) {
            // Batch data and the cached Product.quantity have drifted out of sync somehow —
            // fail loudly rather than silently oversell or leave the cache wrong.
            throw new InsufficientStockException(
                    "Batch records for " + product.getName() + " don't have enough stock to cover this");
        }

        product.setQuantity(product.getQuantity() - quantityToDeduct);
        syncProductFromBatches(product);
        productRepository.save(product);
    }

    public List<InventoryBatchDTO> getStockHistory(Long branchId, Long productId) {
        getOwnedProductOrThrow(branchId, productId);
        return inventoryBatchRepository.findByProduct_IdOrderByReceivedDateDesc(productId).stream()
                .map(InventoryBatchDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** Shared "add a batch and update the owning product" logic — see class Javadoc. */
    private InventoryBatch addBatch(Product product, String batchNumber, int quantity, BigDecimal purchasePrice,
                                     LocalDate expiryDate, Supplier supplier, LocalDate receivedDate) {
        InventoryBatch batch = new InventoryBatch(
                batchNumber, product, product.getBranch(), quantity, purchasePrice, expiryDate, supplier, receivedDate);
        batch = inventoryBatchRepository.save(batch);

        product.setQuantity(product.getQuantity() + quantity);
        syncProductFromBatches(product);
        productRepository.save(product);
        return batch;
    }

    /**
     * Recomputes product.expiryDate from the earliest-expiring batch that still has stock.
     * Products with no batch history at all (shouldn't happen going forward, since creation
     * always makes an opening batch) are left untouched rather than having their expiry wiped.
     */
    private void syncProductFromBatches(Product product) {
        List<InventoryBatch> batches = inventoryBatchRepository.findAvailableFifoOrder(product.getId());
        if (batches.isEmpty()) {
            List<InventoryBatch> anyBatchesEver = inventoryBatchRepository.findByProduct_IdOrderByReceivedDateDesc(product.getId());
            if (anyBatchesEver.isEmpty()) {
                return; // no batch history for this product — leave its expiryDate as manager-set
            }
            product.setExpiryDate(null); // every batch is depleted — nothing dated left to track
            return;
        }
        product.setExpiryDate(batches.get(0).getExpiryDate());
    }

    private Product getOwnedProductOrThrow(Long branchId, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        if (!product.getBranch().getId().equals(branchId)) {
            throw new ProductNotFoundException(productId);
        }
        return product;
    }
}
