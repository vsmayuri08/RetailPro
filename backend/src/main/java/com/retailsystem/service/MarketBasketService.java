package com.retailsystem.service;

import com.retailsystem.dto.MarketBasketSummaryDTO;
import com.retailsystem.dto.ProductPairDTO;
import com.retailsystem.entity.Sale;
import com.retailsystem.entity.SaleItem;
import com.retailsystem.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Market basket analysis — a novel extension next to ABC analysis: find
 * which products are frequently bought together in the same Sale so a
 * manager can spot cross-sell opportunities (e.g. "68% of customers who
 * bought Bread also bought Butter").
 *
 * For each directed pair A → B that co-occurs at least {@code minSupport}
 * times: support is the share of all transactions containing both;
 * confidence is the share of A's transactions that also contain B.
 */
@Service
public class MarketBasketService {

    private static final int DEFAULT_LOOKBACK_DAYS = 90;
    private static final int DEFAULT_MIN_SUPPORT = 2;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final String NOT_ENOUGH_DATA =
            "Not enough data yet — market basket analysis needs at least two sales in this period.";

    @Autowired
    private SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public MarketBasketSummaryDTO generateAnalysis(Long branchId, LocalDate startDate, LocalDate endDate,
                                                   Integer minSupportParam) {
        int minSupport = minSupportParam != null && minSupportParam > 0 ? minSupportParam : DEFAULT_MIN_SUPPORT;

        LocalDate periodStart = startDate != null ? startDate : LocalDate.now().minusDays(DEFAULT_LOOKBACK_DAYS);
        LocalDate periodEnd = endDate != null ? endDate : LocalDate.now();
        LocalDateTime rangeStart = periodStart.atStartOfDay();
        LocalDateTime rangeEnd = periodEnd.plusDays(1).atStartOfDay(); // inclusive of periodEnd

        List<Sale> sales = saleRepository.findByBranch_IdAndCreatedAtBetween(branchId, rangeStart, rangeEnd);

        MarketBasketSummaryDTO result = new MarketBasketSummaryDTO();
        result.setPeriodStart(periodStart);
        result.setPeriodEnd(periodEnd);
        result.setMinSupport(minSupport);
        result.setTotalTransactions(sales.size());

        if (sales.size() < 2) {
            result.setPairs(Collections.emptyList());
            result.setMessage(NOT_ENOUGH_DATA);
            return result;
        }

        Map<Long, String> namesByProduct = new HashMap<>();
        Map<Long, Integer> transactionsContaining = new HashMap<>();
        Map<String, Integer> pairCounts = new HashMap<>();

        for (Sale sale : sales) {
            Set<Long> productIds = new LinkedHashSet<>();
            for (SaleItem item : sale.getItems()) {
                if (item.getProduct() == null) {
                    continue;
                }
                Long productId = item.getProduct().getId();
                productIds.add(productId);
                namesByProduct.putIfAbsent(productId, item.getProductNameSnapshot());
            }

            for (Long productId : productIds) {
                transactionsContaining.merge(productId, 1, Integer::sum);
            }

            List<Long> distinct = new ArrayList<>(productIds);
            for (int i = 0; i < distinct.size(); i++) {
                for (int j = i + 1; j < distinct.size(); j++) {
                    pairCounts.merge(unorderedKey(distinct.get(i), distinct.get(j)), 1, Integer::sum);
                }
            }
        }

        int totalTransactions = sales.size();
        List<ProductPairDTO> pairs = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : pairCounts.entrySet()) {
            int timesTogether = entry.getValue();
            if (timesTogether < minSupport) {
                continue;
            }

            String[] parts = entry.getKey().split(":");
            Long idA = Long.valueOf(parts[0]);
            Long idB = Long.valueOf(parts[1]);

            BigDecimal supportPercent = percent(timesTogether, totalTransactions);

            pairs.add(directedPair(
                    namesByProduct.get(idA), namesByProduct.get(idB),
                    timesTogether, supportPercent,
                    percent(timesTogether, transactionsContaining.getOrDefault(idA, 0))));
            pairs.add(directedPair(
                    namesByProduct.get(idB), namesByProduct.get(idA),
                    timesTogether, supportPercent,
                    percent(timesTogether, transactionsContaining.getOrDefault(idB, 0))));
        }

        pairs.sort((a, b) -> b.getConfidencePercent().compareTo(a.getConfidencePercent()));

        result.setPairs(pairs);
        if (pairs.isEmpty()) {
            result.setMessage("No product pairs met the minimum co-occurrence threshold for this period.");
        }
        return result;
    }

    private static ProductPairDTO directedPair(String productAName, String productBName, int timesTogether,
                                               BigDecimal supportPercent, BigDecimal confidencePercent) {
        ProductPairDTO dto = new ProductPairDTO();
        dto.setProductAName(productAName);
        dto.setProductBName(productBName);
        dto.setTimesTogether(timesTogether);
        dto.setSupportPercent(supportPercent);
        dto.setConfidencePercent(confidencePercent);
        return dto;
    }

    private static BigDecimal percent(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return new BigDecimal(numerator)
                .divide(new BigDecimal(denominator), 4, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Canonical key so (A,B) and (B,A) share one co-occurrence count. */
    private static String unorderedKey(Long a, Long b) {
        return a < b ? a + ":" + b : b + ":" + a;
    }
}
