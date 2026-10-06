package com.retailsystem.service;

import com.retailsystem.dto.DemandForecastItemDTO;
import com.retailsystem.dto.DemandForecastSummaryDTO;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.Sale;
import com.retailsystem.entity.SaleItem;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Near-term demand forecast — a novel extension (not part of the original
 * module spec) that sits next to ABC analysis as a read-only report over
 * real sales history. For each active product we:
 * <ul>
 *   <li>average daily units sold over the lookback window (simple moving
 *       average — no ML library, same in-process math style as ABC);</li>
 *   <li>project that pace across the next N days;</li>
 *   <li>compare the first third of the window to the last third to label
 *       demand as RISING / FALLING / STABLE;</li>
 *   <li>flag stockout risk when projected demand exceeds current stock,
 *       with days-until-stockout = stock ÷ daily average. Zero average
 *       demand is treated as "no forecastable stockout", never as a
 *       divide-by-zero.</li>
 * </ul>
 * Products with no sales in the window are kept on purpose — absence of
 * demand is itself a signal, matching the ABC "dead stock" principle.
 */
@Service
public class DemandForecastService {

    private static final int DEFAULT_LOOKBACK_DAYS = 90;
    private static final int DEFAULT_FORECAST_DAYS = 7;
    private static final int MIN_FORECAST_DAYS = 1;
    private static final int MAX_FORECAST_DAYS = 90;
    /** Relative change between early and late thirds needed to call a trend (10%). */
    private static final BigDecimal TREND_THRESHOLD = new BigDecimal("0.10");

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional(readOnly = true)
    public DemandForecastSummaryDTO generateForecast(Long branchId, LocalDate startDate, LocalDate endDate,
                                                     Integer forecastDaysParam) {
        int forecastDays = forecastDaysParam != null ? forecastDaysParam : DEFAULT_FORECAST_DAYS;
        if (forecastDays < MIN_FORECAST_DAYS || forecastDays > MAX_FORECAST_DAYS) {
            throw new BadRequestException("forecastDays must be between " + MIN_FORECAST_DAYS
                    + " and " + MAX_FORECAST_DAYS);
        }

        LocalDate periodStart = startDate != null ? startDate : LocalDate.now().minusDays(DEFAULT_LOOKBACK_DAYS);
        LocalDate periodEnd = endDate != null ? endDate : LocalDate.now();
        if (periodEnd.isBefore(periodStart)) {
            throw new BadRequestException("endDate must be on or after startDate");
        }

        LocalDateTime rangeStart = periodStart.atStartOfDay();
        LocalDateTime rangeEnd = periodEnd.plusDays(1).atStartOfDay(); // inclusive of periodEnd
        int lookbackDays = (int) ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        BigDecimal lookbackDaysBd = new BigDecimal(lookbackDays);

        List<Sale> sales = saleRepository.findByBranch_IdAndCreatedAtBetween(branchId, rangeStart, rangeEnd);

        Map<Long, Integer> unitsByProduct = new HashMap<>();
        Map<Long, Map<LocalDate, Integer>> dailyUnitsByProduct = new HashMap<>();
        for (Sale sale : sales) {
            LocalDate saleDay = sale.getCreatedAt().toLocalDate();
            for (SaleItem item : sale.getItems()) {
                Long productId = item.getProduct().getId();
                unitsByProduct.merge(productId, item.getQuantity(), Integer::sum);
                dailyUnitsByProduct
                        .computeIfAbsent(productId, k -> new HashMap<>())
                        .merge(saleDay, item.getQuantity(), Integer::sum);
            }
        }

        List<Product> products = productRepository.findByBranchIdAndActiveTrue(branchId);
        List<DemandForecastItemDTO> items = new ArrayList<>();
        int stockoutRiskCount = 0;
        int risingCount = 0;
        int fallingCount = 0;
        int stableCount = 0;
        int productsWithNoSales = 0;

        for (Product product : products) {
            int unitsSold = unitsByProduct.getOrDefault(product.getId(), 0);
            BigDecimal averageDaily = new BigDecimal(unitsSold)
                    .divide(lookbackDaysBd, 4, RoundingMode.HALF_UP);
            BigDecimal forecastedDemand = averageDaily
                    .multiply(new BigDecimal(forecastDays))
                    .setScale(2, RoundingMode.HALF_UP);

            String trend = computeTrend(periodStart, periodEnd, lookbackDays,
                    dailyUnitsByProduct.get(product.getId()));

            int currentStock = product.getQuantity();
            Integer daysUntilStockout = null;
            if (averageDaily.compareTo(BigDecimal.ZERO) > 0) {
                daysUntilStockout = new BigDecimal(currentStock)
                        .divide(averageDaily, 0, RoundingMode.DOWN)
                        .intValue();
            }

            boolean stockoutRisk = forecastedDemand.compareTo(new BigDecimal(currentStock)) > 0
                    && averageDaily.compareTo(BigDecimal.ZERO) > 0;

            DemandForecastItemDTO item = new DemandForecastItemDTO();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setSku(product.getSku());
            item.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
            item.setUnitsSold(unitsSold);
            item.setAverageDailyUnitsSold(averageDaily.setScale(2, RoundingMode.HALF_UP));
            item.setForecastedDemand(forecastedDemand);
            item.setTrend(trend);
            item.setCurrentStock(currentStock);
            item.setEstimatedDaysUntilStockout(daysUntilStockout);
            item.setStockoutRisk(stockoutRisk);
            item.setHasSales(unitsSold > 0);

            items.add(item);
            if (stockoutRisk) {
                stockoutRiskCount++;
            }
            if (!item.isHasSales()) {
                productsWithNoSales++;
            }
            switch (trend) {
                case "RISING" -> risingCount++;
                case "FALLING" -> fallingCount++;
                default -> stableCount++;
            }
        }

        // At-risk products first (the actionable list), then highest forecasted demand.
        items.sort((a, b) -> {
            if (a.isStockoutRisk() != b.isStockoutRisk()) {
                return a.isStockoutRisk() ? -1 : 1;
            }
            int demandCmp = b.getForecastedDemand().compareTo(a.getForecastedDemand());
            if (demandCmp != 0) {
                return demandCmp;
            }
            return a.getProductName().compareToIgnoreCase(b.getProductName());
        });

        DemandForecastSummaryDTO result = new DemandForecastSummaryDTO();
        result.setPeriodStart(periodStart);
        result.setPeriodEnd(periodEnd);
        result.setLookbackDays(lookbackDays);
        result.setForecastDays(forecastDays);
        result.setTotalProducts(products.size());
        result.setStockoutRiskCount(stockoutRiskCount);
        result.setRisingCount(risingCount);
        result.setFallingCount(fallingCount);
        result.setStableCount(stableCount);
        result.setProductsWithNoSales(productsWithNoSales);
        result.setItems(items);
        return result;
    }

    /**
     * Compare average daily units in the earliest third of the window vs the
     * most recent third. A 10% relative move is enough to call RISING or
     * FALLING; anything smaller (or a window too short to split into thirds)
     * is STABLE. New demand appearing after a silent early period is RISING.
     */
    private String computeTrend(LocalDate periodStart, LocalDate periodEnd, int lookbackDays,
                                Map<LocalDate, Integer> dailyUnits) {
        int thirdSize = lookbackDays / 3;
        if (thirdSize < 1) {
            return "STABLE";
        }

        LocalDate earlyEnd = periodStart.plusDays(thirdSize - 1L);
        LocalDate lateStart = periodEnd.minusDays(thirdSize - 1L);
        int earlyUnits = 0;
        int lateUnits = 0;
        if (dailyUnits != null) {
            for (Map.Entry<LocalDate, Integer> entry : dailyUnits.entrySet()) {
                LocalDate day = entry.getKey();
                if (!day.isBefore(periodStart) && !day.isAfter(earlyEnd)) {
                    earlyUnits += entry.getValue();
                }
                if (!day.isBefore(lateStart) && !day.isAfter(periodEnd)) {
                    lateUnits += entry.getValue();
                }
            }
        }

        BigDecimal thirdSizeBd = new BigDecimal(thirdSize);
        BigDecimal earlyAvg = new BigDecimal(earlyUnits).divide(thirdSizeBd, 4, RoundingMode.HALF_UP);
        BigDecimal lateAvg = new BigDecimal(lateUnits).divide(thirdSizeBd, 4, RoundingMode.HALF_UP);

        if (earlyAvg.compareTo(BigDecimal.ZERO) == 0) {
            return lateAvg.compareTo(BigDecimal.ZERO) > 0 ? "RISING" : "STABLE";
        }

        BigDecimal relativeChange = lateAvg.subtract(earlyAvg).divide(earlyAvg, 4, RoundingMode.HALF_UP);
        if (relativeChange.compareTo(TREND_THRESHOLD) >= 0) {
            return "RISING";
        }
        if (relativeChange.compareTo(TREND_THRESHOLD.negate()) <= 0) {
            return "FALLING";
        }
        return "STABLE";
    }
}
