package com.retailsystem.service;

import com.retailsystem.dto.AbcAnalysisItemDTO;
import com.retailsystem.dto.AbcAnalysisSummaryDTO;
import com.retailsystem.dto.AbcClassSummaryDTO;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.Sale;
import com.retailsystem.entity.SaleItem;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ABC inventory analysis — a classic technique (not part of the original
 * module spec, added as a novel extension): rank products by how much
 * revenue they actually generated, then classify them:
 * <ul>
 *   <li><b>A</b> — the small set of products responsible for roughly the
 *       first 80% of revenue. These matter most for reorder priority.</li>
 *   <li><b>B</b> — the next tier, up to roughly 95% of cumulative revenue.</li>
 *   <li><b>C</b> — the long tail, including any product with zero sales in
 *       the period at all (dead stock — a manager overstocking a Class C
 *       item is a concrete, actionable red flag this surfaces).</li>
 * </ul>
 *
 * Classification uses the standard convention of comparing each product's
 * cumulative revenue <i>before</i> it (i.e. everything ranked above it) to
 * the 80%/95% thresholds, not cumulative revenue including it — this is
 * what correctly puts a single dominant product in Class A even though
 * including it alone might push cumulative past 80%.
 */
@Service
public class AbcAnalysisService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal CLASS_A_THRESHOLD = new BigDecimal("80");
    private static final BigDecimal CLASS_B_THRESHOLD = new BigDecimal("95");

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional(readOnly = true)
    public AbcAnalysisSummaryDTO generateAnalysis(Long branchId, LocalDate startDate, LocalDate endDate) {
        LocalDate periodStart = startDate != null ? startDate : LocalDate.now().minusDays(90);
        LocalDate periodEnd = endDate != null ? endDate : LocalDate.now();
        LocalDateTime rangeStart = periodStart.atStartOfDay();
        LocalDateTime rangeEnd = periodEnd.plusDays(1).atStartOfDay(); // inclusive of periodEnd

        List<Sale> sales = saleRepository.findByBranch_IdAndCreatedAtBetween(branchId, rangeStart, rangeEnd);

        Map<Long, BigDecimal> revenueByProduct = new HashMap<>();
        Map<Long, Integer> unitsByProduct = new HashMap<>();
        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                Long productId = item.getProduct().getId();
                revenueByProduct.merge(productId, item.getLineTotal(), BigDecimal::add);
                unitsByProduct.merge(productId, item.getQuantity(), Integer::sum);
            }
        }

        // Every active product is included, even ones with zero revenue in the period — those
        // are exactly the dead-stock candidates this analysis is meant to surface.
        List<Product> products = productRepository.findByBranchIdAndActiveTrue(branchId);
        List<Product> rankedProducts = products.stream()
                .sorted((a, b) -> revenueByProduct.getOrDefault(b.getId(), BigDecimal.ZERO)
                        .compareTo(revenueByProduct.getOrDefault(a.getId(), BigDecimal.ZERO)))
                .collect(Collectors.toList());

        BigDecimal totalRevenue = revenueByProduct.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean hasAnyRevenue = totalRevenue.compareTo(BigDecimal.ZERO) > 0;

        Map<String, List<AbcAnalysisItemDTO>> byClass = new LinkedHashMap<>();
        byClass.put("A", new ArrayList<>());
        byClass.put("B", new ArrayList<>());
        byClass.put("C", new ArrayList<>());

        List<AbcAnalysisItemDTO> items = new ArrayList<>();
        BigDecimal cumulativeRevenue = BigDecimal.ZERO;
        int rank = 0;

        for (Product product : rankedProducts) {
            rank++;
            BigDecimal revenue = revenueByProduct.getOrDefault(product.getId(), BigDecimal.ZERO);
            int units = unitsByProduct.getOrDefault(product.getId(), 0);

            BigDecimal cumulativeBeforePercent = hasAnyRevenue
                    ? cumulativeRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(HUNDRED)
                    : BigDecimal.ZERO;

            String abcClass;
            if (!hasAnyRevenue) {
                abcClass = "C"; // nothing sold at all in the period — nothing to rank
            } else if (cumulativeBeforePercent.compareTo(CLASS_A_THRESHOLD) < 0) {
                abcClass = "A";
            } else if (cumulativeBeforePercent.compareTo(CLASS_B_THRESHOLD) < 0) {
                abcClass = "B";
            } else {
                abcClass = "C";
            }

            cumulativeRevenue = cumulativeRevenue.add(revenue);
            BigDecimal cumulativeAfterPercent = hasAnyRevenue
                    ? cumulativeRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(HUNDRED)
                    : BigDecimal.ZERO;
            BigDecimal revenuePercent = hasAnyRevenue
                    ? revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(HUNDRED)
                    : BigDecimal.ZERO;

            AbcAnalysisItemDTO item = new AbcAnalysisItemDTO();
            item.setRank(rank);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setSku(product.getSku());
            item.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
            item.setUnitsSold(units);
            item.setRevenue(revenue.setScale(2, RoundingMode.HALF_UP));
            item.setRevenuePercent(revenuePercent.setScale(2, RoundingMode.HALF_UP));
            item.setCumulativeRevenuePercent(cumulativeAfterPercent.setScale(2, RoundingMode.HALF_UP));
            item.setAbcClass(abcClass);
            item.setHasSales(revenue.compareTo(BigDecimal.ZERO) > 0);
            item.setCurrentStock(product.getQuantity());
            item.setStockValue(product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity())).setScale(2, RoundingMode.HALF_UP));

            items.add(item);
            byClass.get(abcClass).add(item);
        }

        List<AbcClassSummaryDTO> classSummaries = new ArrayList<>();
        for (Map.Entry<String, List<AbcAnalysisItemDTO>> entry : byClass.entrySet()) {
            List<AbcAnalysisItemDTO> classItems = entry.getValue();
            BigDecimal classRevenue = classItems.stream().map(AbcAnalysisItemDTO::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal classStockValue = classItems.stream().map(AbcAnalysisItemDTO::getStockValue).reduce(BigDecimal.ZERO, BigDecimal::add);

            AbcClassSummaryDTO summary = new AbcClassSummaryDTO();
            summary.setAbcClass(entry.getKey());
            summary.setItemCount(classItems.size());
            summary.setItemCountPercent(rankedProducts.isEmpty() ? BigDecimal.ZERO
                    : new BigDecimal(classItems.size()).divide(new BigDecimal(rankedProducts.size()), 4, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP));
            summary.setRevenue(classRevenue.setScale(2, RoundingMode.HALF_UP));
            summary.setRevenuePercent(hasAnyRevenue
                    ? classRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            summary.setStockValue(classStockValue);
            classSummaries.add(summary);
        }

        AbcAnalysisSummaryDTO result = new AbcAnalysisSummaryDTO();
        result.setPeriodStart(periodStart);
        result.setPeriodEnd(periodEnd);
        result.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        result.setTotalProducts(rankedProducts.size());
        result.setClassSummaries(classSummaries);
        result.setItems(items);
        return result;
    }
}
