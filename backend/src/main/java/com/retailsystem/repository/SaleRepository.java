package com.retailsystem.repository;

import com.retailsystem.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    boolean existsByInvoiceNumber(String invoiceNumber);
    long countByBranch_IdAndCreatedAtBetween(Long branchId, LocalDateTime start, LocalDateTime end);
    List<Sale> findByBranch_IdOrderByCreatedAtDesc(Long branchId);
    List<Sale> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    /** Module 12 -> org-wide reporting: every sale in a date range, across all branches. */
    List<Sale> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /** ABC Analysis -> a single branch's sales within a date range. */
    List<Sale> findByBranch_IdAndCreatedAtBetween(Long branchId, LocalDateTime start, LocalDateTime end);

    java.util.Optional<Sale> findByInvoiceNumber(String invoiceNumber);
}
