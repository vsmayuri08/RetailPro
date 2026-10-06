package com.retailsystem.repository;

import com.retailsystem.entity.StockTransfer;
import com.retailsystem.enums.StockTransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    /** Requests where the given branch is the SOURCE (i.e. that branch's manager must decide). */
    List<StockTransfer> findByProduct_Branch_IdOrderByCreatedAtDesc(Long branchId);

    List<StockTransfer> findByProduct_Branch_IdAndStatusOrderByCreatedAtDesc(Long branchId, StockTransferStatus status);

    /** Requests the given branch itself raised (it's the destination). */
    List<StockTransfer> findByDestinationBranch_IdOrderByCreatedAtDesc(Long branchId);

    long countByProduct_Branch_IdAndStatus(Long branchId, StockTransferStatus status);

    long countByDestinationBranch_IdAndStatus(Long branchId, StockTransferStatus status);
}
