package com.retailsystem.repository;

import com.retailsystem.entity.InventoryBatch;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    /**
     * Module 5 -> "use the earliest-expiry batch first when selling". Batches with an expiry
     * date come first (soonest first); batches with no expiry date are exhausted last, since
     * they're in no danger of going to waste. Written as a portable JPQL CASE expression rather
     * than relying on a specific database's NULL-ordering convention for ASC sorts, which differs
     * between MySQL and other databases.
     */
    @Query("SELECT b FROM InventoryBatch b WHERE b.product.id = :productId AND b.availableQuantity > 0 "
            + "ORDER BY CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END ASC, b.expiryDate ASC, b.createdAt ASC")
    List<InventoryBatch> findAvailableFifoOrder(@Param("productId") Long productId);

    List<InventoryBatch> findByProduct_IdOrderByReceivedDateDesc(Long productId);

    /** Reorder suggestions — all batches for a branch, with supplier, so we can pick a vendor from real receipts. */
    @Query("SELECT b FROM InventoryBatch b JOIN FETCH b.product LEFT JOIN FETCH b.supplier WHERE b.branch.id = :branchId")
    List<InventoryBatch> findByBranchIdWithSupplier(@Param("branchId") Long branchId);
}
