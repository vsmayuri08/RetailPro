package com.retailsystem.repository;

import com.retailsystem.entity.SaleReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {

    List<SaleReturn> findBySale_Id(Long saleId);

    List<SaleReturn> findBySale_IdIn(Collection<Long> saleIds);

    List<SaleReturn> findBySale_Branch_IdOrderByCreatedAtDesc(Long branchId);
}
