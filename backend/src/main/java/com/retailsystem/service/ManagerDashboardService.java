package com.retailsystem.service;

import com.retailsystem.dto.ManagerDashboardStatsDTO;
import com.retailsystem.entity.Product;
import com.retailsystem.enums.Role;
import com.retailsystem.enums.StockTransferStatus;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.StockTransferRepository;
import com.retailsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ManagerDashboardService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockTransferRepository stockTransferRepository;

    public ManagerDashboardStatsDTO getStats(Long branchId) {
        List<Product> products = productRepository.findByBranchIdAndActiveTrue(branchId);

        ManagerDashboardStatsDTO stats = new ManagerDashboardStatsDTO();
        stats.setTotalProducts(products.size());
        stats.setLowStockCount(products.stream().filter(p -> p.getQuantity() <= p.getReorderLevel()).count());
        stats.setNearExpiryCount(products.stream()
                .filter(p -> p.getExpiryDate() != null
                        && !p.getExpiryDate().isBefore(java.time.LocalDate.now())
                        && !p.getExpiryDate().isAfter(java.time.LocalDate.now().plusDays(14)))
                .count());
        stats.setTotalEmployees(userRepository.findByBranchIdAndRole(branchId, Role.CASHIER).size());
        stats.setPendingIncomingTransfers(stockTransferRepository.countByProduct_Branch_IdAndStatus(branchId, StockTransferStatus.PENDING));
        stats.setPendingOutgoingTransfers(stockTransferRepository.countByDestinationBranch_IdAndStatus(branchId, StockTransferStatus.PENDING));
        stats.setStockValue(products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return stats;
    }
}
