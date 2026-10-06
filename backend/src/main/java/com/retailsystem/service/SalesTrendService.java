package com.retailsystem.service;

import com.retailsystem.dto.SalesTrendPointDTO;
import com.retailsystem.dto.SalesTrendSummaryDTO;
import com.retailsystem.entity.Sale;
import com.retailsystem.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Monthly revenue history plus next-month prediction. All figures come from
 * real Sale.totalAmount rows — the model does not invent revenue.
 */
@Service
public class SalesTrendService {

    private static final int HISTORY_MONTHS = 6;
    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    @Autowired
    private SaleRepository saleRepository;

    /**
     * @param branchId null means organization-wide (Super Admin)
     */
    @Transactional(readOnly = true)
    public SalesTrendSummaryDTO generate(Long branchId) {
        LocalDate today = LocalDate.now();
        YearMonth current = YearMonth.from(today);
        YearMonth windowStart = current.minusMonths(HISTORY_MONTHS - 1);
        LocalDateTime from = windowStart.atDay(1).atStartOfDay();
        LocalDateTime to = current.plusMonths(1).atDay(1).atStartOfDay();

        List<Sale> sales = branchId == null
                ? saleRepository.findByCreatedAtBetween(from, to)
                : saleRepository.findByBranch_IdAndCreatedAtBetween(branchId, from, to);

        Map<YearMonth, BigDecimal> byMonth = sales.stream().collect(Collectors.groupingBy(
                s -> YearMonth.from(s.getCreatedAt()),
                Collectors.mapping(Sale::getTotalAmount,
                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        List<SalesTrendPointDTO> points = new ArrayList<>();
        List<BigDecimal> complete = new ArrayList<>();
        BigDecimal currentMtd = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal lastComplete = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        for (int i = 0; i < HISTORY_MONTHS; i++) {
            YearMonth ym = windowStart.plusMonths(i);
            BigDecimal revenue = byMonth.getOrDefault(ym, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            boolean isCurrent = ym.equals(current);
            SalesTrendPointDTO point = new SalesTrendPointDTO();
            point.setYearMonth(ym.toString());
            point.setLabel(isCurrent ? ym.format(LABEL) + " (MTD)" : ym.format(LABEL));
            point.setRevenue(revenue);
            point.setKind(isCurrent ? "CURRENT" : "HISTORICAL");
            points.add(point);
            if (isCurrent) {
                currentMtd = revenue;
            } else {
                complete.add(revenue);
                lastComplete = revenue;
            }
        }

        String method;
        BigDecimal predicted;
        List<BigDecimal> completeWithSales = complete.stream()
                .filter(v -> v.compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        if (completeWithSales.size() >= 2) {
            predicted = linearNext(completeWithSales);
            method = "Linear trend of the last " + completeWithSales.size() + " complete months with sales";
        } else if (currentMtd.compareTo(BigDecimal.ZERO) > 0) {
            int elapsed = Math.max(1, today.getDayOfMonth());
            BigDecimal daily = currentMtd.divide(BigDecimal.valueOf(elapsed), 8, RoundingMode.HALF_UP);
            predicted = daily.multiply(BigDecimal.valueOf(current.plusMonths(1).lengthOfMonth()))
                    .setScale(2, RoundingMode.HALF_UP);
            method = "Current-month daily run rate × days in next month";
        } else if (complete.size() == 1) {
            predicted = complete.get(0);
            method = "Held last complete month (only one month of history)";
        } else {
            predicted = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            method = "No sales in the lookback window";
        }

        YearMonth next = current.plusMonths(1);
        SalesTrendPointDTO future = new SalesTrendPointDTO();
        future.setYearMonth(next.toString());
        future.setLabel(next.format(LABEL));
        future.setRevenue(predicted);
        future.setKind("PREDICTED");
        points.add(future);

        BigDecimal baseline = lastComplete.compareTo(BigDecimal.ZERO) > 0 ? lastComplete : currentMtd;
        BigDecimal growth = null;
        if (baseline.compareTo(BigDecimal.ZERO) > 0) {
            growth = predicted.subtract(baseline)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(baseline, 1, RoundingMode.HALF_UP);
        }

        SalesTrendSummaryDTO summary = new SalesTrendSummaryDTO();
        summary.setPoints(points);
        summary.setPredictedNextMonthRevenue(predicted);
        summary.setExpectedGrowthPercent(growth);
        summary.setLastCompleteMonthRevenue(lastComplete);
        summary.setCurrentMonthToDateRevenue(currentMtd);
        summary.setMethod(method);
        summary.setEnoughHistory(sales.stream().anyMatch(s -> s.getTotalAmount().compareTo(BigDecimal.ZERO) > 0));
        return summary;
    }

    private static BigDecimal linearNext(List<BigDecimal> ys) {
        int n = ys.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double y = ys.get(i).doubleValue();
            sumX += i;
            sumY += y;
            sumXY += i * y;
            sumX2 += (double) i * i;
        }
        double denom = n * sumX2 - sumX * sumX;
        double slope = denom == 0 ? 0 : (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;
        double pred = intercept + slope * n;
        if (pred < 0) {
            pred = 0;
        }
        return BigDecimal.valueOf(pred).setScale(2, RoundingMode.HALF_UP);
    }
}
