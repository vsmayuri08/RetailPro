package com.retailsystem.service;

import com.retailsystem.dto.BranchDTO;
import com.retailsystem.dto.BranchRequest;
import com.retailsystem.dto.BranchSalesSummaryDTO;
import com.retailsystem.dto.OrgStatsDTO;
import com.retailsystem.entity.Branch;
import com.retailsystem.entity.Sale;
import com.retailsystem.entity.User;
import com.retailsystem.enums.BranchStatus;
import com.retailsystem.enums.Role;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.repository.SaleRepository;
import com.retailsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Super Admin -> branch management: create, edit, activate/deactivate,
 * and view every branch across the organization.
 */
@Service
public class BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Transactional
    public BranchDTO createBranch(BranchRequest request) {
        if (branchRepository.existsByBranchCode(request.getBranchCode())) {
            throw new BadRequestException("A branch with code '" + request.getBranchCode() + "' already exists");
        }
        Branch branch = new Branch(
                request.getBranchName(),
                request.getBranchCode().toUpperCase(),
                request.getAddress(),
                request.getCity(),
                request.getPhone(),
                BranchStatus.ACTIVE
        );
        branch = branchRepository.save(branch);
        return enrich(BranchDTO.fromEntity(branch), branch);
    }

    @Transactional
    public BranchDTO updateBranch(Long id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        if (!branch.getBranchCode().equalsIgnoreCase(request.getBranchCode())
                && branchRepository.existsByBranchCode(request.getBranchCode())) {
            throw new BadRequestException("A branch with code '" + request.getBranchCode() + "' already exists");
        }

        branch.setBranchName(request.getBranchName());
        branch.setBranchCode(request.getBranchCode().toUpperCase());
        branch.setAddress(request.getAddress());
        branch.setCity(request.getCity());
        branch.setPhone(request.getPhone());
        branch = branchRepository.save(branch);
        return enrich(BranchDTO.fromEntity(branch), branch);
    }

    @Transactional
    public BranchDTO setBranchStatus(Long id, boolean active) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        branch.setStatus(active ? BranchStatus.ACTIVE : BranchStatus.INACTIVE);
        branch = branchRepository.save(branch);
        return enrich(BranchDTO.fromEntity(branch), branch);
    }

    public BranchDTO getBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return enrich(BranchDTO.fromEntity(branch), branch);
    }

    public List<BranchDTO> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(b -> enrich(BranchDTO.fromEntity(b), b))
                .collect(Collectors.toList());
    }

    public OrgStatsDTO getOrgStats() {
        List<Branch> allBranches = branchRepository.findAll();
        List<BranchDTO> branchDTOs = allBranches.stream()
                .map(b -> enrich(BranchDTO.fromEntity(b), b))
                .collect(Collectors.toList());

        OrgStatsDTO stats = new OrgStatsDTO();
        stats.setTotalBranches(allBranches.size());
        stats.setActiveBranches(allBranches.stream().filter(b -> b.getStatus() == BranchStatus.ACTIVE).count());
        stats.setInactiveBranches(allBranches.stream().filter(b -> b.getStatus() == BranchStatus.INACTIVE).count());
        stats.setTotalManagers(userRepository.findByRole(Role.BRANCH_MANAGER).size());
        stats.setTotalCashiers(userRepository.findByRole(Role.CASHIER).size());
        stats.setTotalEmployees(stats.getTotalManagers() + stats.getTotalCashiers());
        stats.setBranches(branchDTOs);

        // Module 12 -> org-wide sales, aggregated live from real Sale records — never seeded/fake.
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        List<Sale> monthSales = saleRepository.findByCreatedAtBetween(startOfMonth, startOfTomorrow);
        List<Sale> todaySales = monthSales.stream()
                .filter(s -> !s.getCreatedAt().isBefore(startOfToday))
                .collect(Collectors.toList());

        stats.setTotalSalesToday(todaySales.size());
        stats.setTotalRevenueToday(sumTotals(todaySales));
        stats.setTotalSalesThisMonth(monthSales.size());
        stats.setTotalRevenueThisMonth(sumTotals(monthSales));

        List<BranchSalesSummaryDTO> branchSales = allBranches.stream()
                .map(branch -> {
                    BranchSalesSummaryDTO summary = new BranchSalesSummaryDTO();
                    summary.setBranchId(branch.getId());
                    summary.setBranchName(branch.getBranchName());

                    List<Sale> branchMonthSales = monthSales.stream()
                            .filter(s -> s.getBranch().getId().equals(branch.getId()))
                            .collect(Collectors.toList());
                    List<Sale> branchTodaySales = branchMonthSales.stream()
                            .filter(s -> !s.getCreatedAt().isBefore(startOfToday))
                            .collect(Collectors.toList());

                    summary.setSalesCountToday(branchTodaySales.size());
                    summary.setRevenueToday(sumTotals(branchTodaySales));
                    summary.setSalesCountThisMonth(branchMonthSales.size());
                    summary.setRevenueThisMonth(sumTotals(branchMonthSales));
                    return summary;
                })
                .collect(Collectors.toList());
        stats.setBranchSales(branchSales);

        return stats;
    }

    private BigDecimal sumTotals(List<Sale> sales) {
        return sales.stream().map(Sale::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BranchDTO enrich(BranchDTO dto, Branch branch) {
        List<User> staff = userRepository.findByBranchId(branch.getId());
        dto.setManagerCount((int) staff.stream().filter(u -> u.getRole() == Role.BRANCH_MANAGER).count());
        dto.setCashierCount((int) staff.stream().filter(u -> u.getRole() == Role.CASHIER).count());
        dto.setEmployeeCount(staff.size());
        return dto;
    }
}
