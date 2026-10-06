package com.retailsystem.service;

import com.retailsystem.dto.CashierLeaderboardEntryDTO;
import com.retailsystem.dto.CashierLeaderboardSummaryDTO;
import com.retailsystem.entity.Sale;
import com.retailsystem.entity.SaleItem;
import com.retailsystem.entity.User;
import com.retailsystem.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CashierLeaderboardService {

    @Autowired
    private SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public CashierLeaderboardSummaryDTO thisMonth(Long branchId) {
        LocalDate periodEnd = LocalDate.now();
        LocalDate periodStart = periodEnd.withDayOfMonth(1);
        LocalDateTime rangeStart = periodStart.atStartOfDay();
        LocalDateTime rangeEnd = periodEnd.plusDays(1).atStartOfDay();

        List<Sale> sales = branchId != null
                ? saleRepository.findByBranch_IdAndCreatedAtBetween(branchId, rangeStart, rangeEnd)
                : saleRepository.findByCreatedAtBetween(rangeStart, rangeEnd);

        Map<Long, CashierLeaderboardEntryDTO> byCashier = new HashMap<>();
        for (Sale sale : sales) {
            User cashier = sale.getProcessedBy();
            if (cashier == null) {
                continue;
            }
            CashierLeaderboardEntryDTO entry = byCashier.computeIfAbsent(cashier.getId(), id -> {
                CashierLeaderboardEntryDTO dto = new CashierLeaderboardEntryDTO();
                dto.setCashierId(id);
                dto.setCashierName(cashier.getFullName());
                dto.setSalesCount(0);
                dto.setItemsSold(0);
                dto.setRevenue(BigDecimal.ZERO);
                return dto;
            });
            entry.setSalesCount(entry.getSalesCount() + 1);
            entry.setRevenue(entry.getRevenue().add(sale.getTotalAmount()));
            int items = 0;
            for (SaleItem item : sale.getItems()) {
                items += item.getQuantity();
            }
            entry.setItemsSold(entry.getItemsSold() + items);
        }

        List<CashierLeaderboardEntryDTO> entries = new ArrayList<>(byCashier.values());
        entries.sort(Comparator
                .comparing(CashierLeaderboardEntryDTO::getRevenue).reversed()
                .thenComparing(CashierLeaderboardEntryDTO::getSalesCount, Comparator.reverseOrder())
                .thenComparing(CashierLeaderboardEntryDTO::getCashierName, String.CASE_INSENSITIVE_ORDER));

        int rank = 1;
        for (CashierLeaderboardEntryDTO entry : entries) {
            entry.setRank(rank++);
            entry.setRevenue(entry.getRevenue().setScale(2, RoundingMode.HALF_UP));
        }

        CashierLeaderboardSummaryDTO result = new CashierLeaderboardSummaryDTO();
        result.setPeriodStart(periodStart);
        result.setPeriodEnd(periodEnd);
        result.setEntries(entries);
        result.setTopPerformerName(entries.isEmpty() ? null : entries.get(0).getCashierName());
        return result;
    }
}
