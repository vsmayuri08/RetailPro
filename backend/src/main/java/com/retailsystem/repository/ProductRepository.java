package com.retailsystem.repository;

import com.retailsystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByBranchId(Long branchId);
    List<Product> findByBranchIdAndActiveTrue(Long branchId);
    boolean existsByBranchIdAndSkuIgnoreCase(Long branchId, String sku);
    Optional<Product> findByBranchIdAndSkuIgnoreCase(Long branchId, String sku);
    Optional<Product> findByBranchIdAndBarcodeIgnoreCase(Long branchId, String barcode);
    List<Product> findByBranchIdAndActiveTrueAndNameContainingIgnoreCase(Long branchId, String name);

    /** Module 3 / Module 9: "search by name, SKU, or barcode" — one query matches any of the three. */
    @Query("SELECT p FROM Product p WHERE p.branch.id = :branchId AND p.active = true "
            + "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchActiveByNameOrSku(@Param("branchId") Long branchId, @Param("query") String query);
}

