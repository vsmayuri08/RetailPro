package com.retailsystem.service;

import com.retailsystem.dto.RfmCustomerDTO;
import com.retailsystem.dto.RfmSegmentSummaryDTO;
import com.retailsystem.dto.RfmSummaryDTO;
import com.retailsystem.entity.Customer;
import com.retailsystem.entity.Sale;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RFM customer segmentation — a novel extension next to ABC analysis.
 * Scores every registered customer who bought at this branch on:
 * <ul>
 *   <li><b>Recency</b> — days since their last sale (lower is better);</li>
 *   <li><b>Frequency</b> — number of sales in the window;</li>
 *   <li><b>Monetary</b> — sum of sale totals.</li>
 * </ul>
 * Each dimension is quintile-scored 1–5, then mapped to a segment
 * (Champions, Loyal, New, Potential, At Risk, Lost) that is sharper than
 * Bronze–Platinum loyalty tiers, which only track cumulative points.
 * Walk-in sales (no customer) are ignored.
 */
@Service
public class RfmAnalysisService {

    private static final int DEFAULT_LOOKBACK_DAYS = 90;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final List<String> SEGMENT_ORDER = Arrays.asList(
            "Champions", "Loyal", "New", "Potential", "At Risk", "Lost");
    private static final String NOT_ENOUGH_DATA =
            "Not enough data yet — RFM needs registered customers with purchases in this period.";

    @Autowired
    private SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public RfmSummaryDTO generateAnalysis(Long branchId, LocalDate startDate, LocalDate endDate) {
        LocalDate periodStart = startDate != null ? startDate : LocalDate.now().minusDays(DEFAULT_LOOKBACK_DAYS);
        LocalDate periodEnd = endDate != null ? endDate : LocalDate.now();
        LocalDateTime rangeStart = periodStart.atStartOfDay();
        LocalDateTime rangeEnd = periodEnd.plusDays(1).atStartOfDay();

        List<Sale> sales = saleRepository.findByBranch_IdAndCreatedAtBetween(branchId, rangeStart, rangeEnd);

        Map<Long, Customer> customersById = new HashMap<>();
        Map<Long, LocalDateTime> lastPurchase = new HashMap<>();
        Map<Long, Integer> frequency = new HashMap<>();
        Map<Long, BigDecimal> monetary = new HashMap<>();

        for (Sale sale : sales) {
            if (sale.getCustomer() == null) {
                continue;
            }
            Customer customer = sale.getCustomer();
            Long id = customer.getId();
            customersById.put(id, customer);
            frequency.merge(id, 1, Integer::sum);
            monetary.merge(id, sale.getTotalAmount(), BigDecimal::add);
            LocalDateTime at = sale.getCreatedAt();
            lastPurchase.merge(id, at, (a, b) -> a.isAfter(b) ? a : b);
        }

        RfmSummaryDTO result = new RfmSummaryDTO();
        result.setPeriodStart(periodStart);
        result.setPeriodEnd(periodEnd);

        if (customersById.size() < 2) {
            result.setTotalCustomers(customersById.size());
            result.setTotalMonetary(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            result.setSegmentSummaries(emptySegmentSummaries());
            result.setCustomers(Collections.emptyList());
            result.setMessage(NOT_ENOUGH_DATA);
            return result;
        }

        List<Integer> recencyValues = new ArrayList<>();
        List<Integer> frequencyValues = new ArrayList<>();
        List<BigDecimal> monetaryValues = new ArrayList<>();
        Map<Long, Integer> recencyDaysById = new HashMap<>();

        for (Long id : customersById.keySet()) {
            int days = (int) ChronoUnit.DAYS.between(lastPurchase.get(id).toLocalDate(), periodEnd);
            recencyDaysById.put(id, Math.max(days, 0));
            recencyValues.add(recencyDaysById.get(id));
            frequencyValues.add(frequency.get(id));
            monetaryValues.add(monetary.get(id));
        }

        List<RfmCustomerDTO> rows = new ArrayList<>();
        BigDecimal totalMonetary = BigDecimal.ZERO;

        for (Long id : customersById.keySet()) {
            Customer customer = customersById.get(id);
            int days = recencyDaysById.get(id);
            int freq = frequency.get(id);
            BigDecimal spend = monetary.get(id).setScale(2, RoundingMode.HALF_UP);

            int rScore = quintileScoreInt(recencyValues, days, false);
            int fScore = quintileScoreInt(frequencyValues, freq, true);
            int mScore = quintileScoreDecimal(monetaryValues, monetary.get(id), true);
            String segment = assignSegment(rScore, fScore, mScore);

            RfmCustomerDTO dto = new RfmCustomerDTO();
            dto.setCustomerId(id);
            dto.setCustomerName(customer.getFullName());
            dto.setPhone(customer.getPhone());
            dto.setLoyaltyTier(customer.getLoyaltyTier() != null ? customer.getLoyaltyTier().name() : null);
            dto.setRecencyDays(days);
            dto.setFrequency(freq);
            dto.setMonetary(spend);
            dto.setRecencyScore(rScore);
            dto.setFrequencyScore(fScore);
            dto.setMonetaryScore(mScore);
            dto.setRfmCode("" + rScore + fScore + mScore);
            dto.setSegment(segment);
            rows.add(dto);
            totalMonetary = totalMonetary.add(spend);
        }

        rows.sort(Comparator
                .comparing(RfmCustomerDTO::getMonetary, Comparator.reverseOrder())
                .thenComparingInt(RfmCustomerDTO::getRecencyDays));

        Map<String, List<RfmCustomerDTO>> bySegment = new LinkedHashMap<>();
        for (String name : SEGMENT_ORDER) {
            bySegment.put(name, new ArrayList<>());
        }
        for (RfmCustomerDTO row : rows) {
            bySegment.get(row.getSegment()).add(row);
        }

        int n = rows.size();
        List<RfmSegmentSummaryDTO> summaries = new ArrayList<>();
        for (String name : SEGMENT_ORDER) {
            List<RfmCustomerDTO> group = bySegment.get(name);
            BigDecimal groupMonetary = group.stream()
                    .map(RfmCustomerDTO::getMonetary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            RfmSegmentSummaryDTO summary = new RfmSegmentSummaryDTO();
            summary.setSegment(name);
            summary.setCustomerCount(group.size());
            summary.setCustomerCountPercent(n == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                    : new BigDecimal(group.size())
                            .divide(new BigDecimal(n), 4, RoundingMode.HALF_UP)
                            .multiply(HUNDRED)
                            .setScale(2, RoundingMode.HALF_UP));
            summary.setTotalMonetary(groupMonetary.setScale(2, RoundingMode.HALF_UP));
            summaries.add(summary);
        }

        result.setTotalCustomers(n);
        result.setTotalMonetary(totalMonetary.setScale(2, RoundingMode.HALF_UP));
        result.setSegmentSummaries(summaries);
        result.setCustomers(rows);
        return result;
    }

    static String assignSegment(int r, int f, int m) {
        if (r >= 4 && f >= 4 && m >= 3) {
            return "Champions";
        }
        if (r >= 4 && f <= 2) {
            return "New";
        }
        if (r <= 2 && f >= 3) {
            return "At Risk";
        }
        if (r <= 2 && f <= 2) {
            return "Lost";
        }
        if (f >= 3 && r >= 3) {
            return "Loyal";
        }
        return "Potential";
    }

    private static int quintileScoreInt(List<Integer> values, int value, boolean higherIsBetter) {
        List<Integer> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int idx = higherIsBetter ? lastIndexOfInt(sorted, value) : firstIndexOfInt(sorted, value);
        return bucketScore(idx, sorted.size(), higherIsBetter);
    }

    private static int quintileScoreDecimal(List<BigDecimal> values, BigDecimal value, boolean higherIsBetter) {
        List<BigDecimal> sorted = new ArrayList<>(values);
        sorted.sort(BigDecimal::compareTo);
        int idx = higherIsBetter ? lastIndexOfDecimal(sorted, value) : firstIndexOfDecimal(sorted, value);
        return bucketScore(idx, sorted.size(), higherIsBetter);
    }

    private static int bucketScore(int idx, int n, boolean higherIsBetter) {
        if (n <= 1) {
            return 5;
        }
        int bucket = Math.min(4, (idx * 5) / n);
        return higherIsBetter ? bucket + 1 : 5 - bucket;
    }

    private static int firstIndexOfInt(List<Integer> sorted, int value) {
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) == value) {
                return i;
            }
        }
        return 0;
    }

    private static int lastIndexOfInt(List<Integer> sorted, int value) {
        for (int i = sorted.size() - 1; i >= 0; i--) {
            if (sorted.get(i) == value) {
                return i;
            }
        }
        return 0;
    }

    private static int firstIndexOfDecimal(List<BigDecimal> sorted, BigDecimal value) {
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).compareTo(value) == 0) {
                return i;
            }
        }
        return 0;
    }

    private static int lastIndexOfDecimal(List<BigDecimal> sorted, BigDecimal value) {
        for (int i = sorted.size() - 1; i >= 0; i--) {
            if (sorted.get(i).compareTo(value) == 0) {
                return i;
            }
        }
        return 0;
    }

    private static List<RfmSegmentSummaryDTO> emptySegmentSummaries() {
        List<RfmSegmentSummaryDTO> summaries = new ArrayList<>();
        for (String name : SEGMENT_ORDER) {
            RfmSegmentSummaryDTO summary = new RfmSegmentSummaryDTO();
            summary.setSegment(name);
            summary.setCustomerCount(0);
            summary.setCustomerCountPercent(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            summary.setTotalMonetary(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            summaries.add(summary);
        }
        return summaries;
    }
}
