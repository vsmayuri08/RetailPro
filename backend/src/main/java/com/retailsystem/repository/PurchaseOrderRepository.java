package com.retailsystem.repository;

import com.retailsystem.entity.PurchaseOrder;
import com.retailsystem.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByBranch_IdOrderByCreatedAtDesc(Long branchId);

    boolean existsByPoNumber(String poNumber);

    long countByBranch_IdAndCreatedAtBetween(Long branchId, LocalDateTime start, LocalDateTime end);

    Optional<PurchaseOrder> findByIdAndBranch_Id(Long id, Long branchId);

    @Query("SELECT DISTINCT i.product.id FROM PurchaseOrderItem i "
            + "WHERE i.purchaseOrder.branch.id = :branchId "
            + "AND i.purchaseOrder.status IN :statuses")
    List<Long> findProductIdsOnOpenOrders(@Param("branchId") Long branchId,
                                          @Param("statuses") Collection<PurchaseOrderStatus> statuses);
}
