package com.retailsystem.repository;

import com.retailsystem.entity.SaleReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SaleReturnItemRepository extends JpaRepository<SaleReturnItem, Long> {

    List<SaleReturnItem> findBySaleReturn_Sale_Id(Long saleId);

    List<SaleReturnItem> findBySaleReturn_Sale_IdIn(Collection<Long> saleIds);
}
