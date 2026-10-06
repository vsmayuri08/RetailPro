package com.retailsystem.service;

import com.retailsystem.dto.ReorderSuggestionItemDTO;
import com.retailsystem.dto.ReorderSuggestionsSummaryDTO;
import com.retailsystem.entity.InventoryBatch;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.Sale;
import com.retailsystem.entity.SaleItem;
import com.retailsystem.entity.Supplier;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.repository.InventoryBatchRepository;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reorder suggestions — a novel extension next to ABC analysis. Instead of
 * flagging {@code Product.reorderLevel} (a fixed catalog number), this
 * computes how many units to order from real sales velocity and which
 * supplier to order from using purchase-batch history.
 *
 * <ul>
 *   <li>Average daily demand = units sold in the lookback window ÷ days.</li>
 *   <li>Lead time = average gap between consecutive supplier receipts for
 *       that product (falls back to 14 days if there aren't two receipts).</li>
 *   <li>Safety cover = half the lead time, at least 3 days.</li>
 *   <li>Suggested qty = ceil(daily × (lead + safety)) − current stock,
 *       never below zero. Catalog reorderLevel is returned only as contrast.</li>
 *   <li>Suggested supplier = the vendor who delivered the most units in
 *       batches that actually have a supplier (opening/transfer/return
 *       batches are ignored because they have none).</li>
 * </ul>
 */
@Service
public class ReorderSuggestionsService {

    private static final int DEFAULT_LOOKBACK_DAYS = 90;
    private static final int DEFAULT_LEAD_TIME_DAYS = 14;
    private static final int MIN_LEAD_TIME_DAYS = 1;
    private static final int MAX_LEAD_TIME_DAYS = 45;
    private static final int MIN_SAFETY_DAYS = 3;
    private static final String NO_REORDER_NEEDED =
            "No reorder needed — current stock covers expected demand through lead time plus safety stock.";
    private static final String NOT_ENOUGH_SALES =
            "Not enough data yet — reorder suggestions need at least one sale in this period.";

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Transactional(readOnly = true)
    public ReorderSuggestionsSummaryDTO generateSuggestions(Long branchId, LocalDate startDate, LocalDate endDate) {
        LocalDate periodStart = startDate != null ? startDate : LocalDate.now().minusDays(DEFAULT_LOOKBACK_DAYS);
        LocalDate periodEnd = endDate != null ? endDate : LocalDate.now();
        if (periodEnd.isBefore(periodStart)) {
            throw new BadRequestException("endDate must be on or after startDate");
        }

        LocalDateTime rangeStart = periodStart.atStartOfDay();
        LocalDateTime rangeEnd = periodEnd.plusDays(1).atStartOfDay();
        int lookbackDays = (int) ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;
        BigDecimal lookbackDaysBd = new BigDecimal(lookbackDays);

        List<Sale> sales = saleRepository.findByBranch_IdAndCreatedAtBetween(branchId, rangeStart, rangeEnd);
        Map<Long, Integer> unitsByProduct = new HashMap<>();
        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                if (item.getProduct() == null) {
                    continue;
                }
                unitsByProduct.merge(item.getProduct().getId(), item.getQuantity(), Integer::sum);
            }
        }

        List<Product> products = productRepository.findByBranchIdAndActiveTrue(branchId);
        Map<Long, List<InventoryBatch>> batchesByProduct = new HashMap<>();
        for (InventoryBatch batch : inventoryBatchRepository.findByBranchIdWithSupplier(branchId)) {
            batchesByProduct.computeIfAbsent(batch.getProduct().getId(), k -> new ArrayList<>()).add(batch);
        }

        List<ReorderSuggestionItemDTO> items = new ArrayList<>();
        int productsWithSales = 0;
        int criticalCount = 0;
        int missingSupplierCount = 0;
        BigDecimal estimatedOrderCost = BigDecimal.ZERO;

        for (Product product : products) {
            int unitsSold = unitsByProduct.getOrDefault(product.getId(), 0);
            if (unitsSold <= 0) {
                continue;
            }
            productsWithSales++;

            BigDecimal averageDaily = new BigDecimal(unitsSold).divide(lookbackDaysBd, 4, RoundingMode.HALF_UP);
            List<InventoryBatch> productBatches = batchesByProduct.getOrDefault(product.getId(), Collections.emptyList());
            List<InventoryBatch> purchaseBatches = purchaseBatches(productBatches);

            int leadTimeDays = estimateLeadTimeDays(purchaseBatches);
            int safetyDays = Math.max(MIN_SAFETY_DAYS, (int) Math.ceil(leadTimeDays / 2.0));
            int coverDays = leadTimeDays + safetyDays;
            int currentStock = product.getQuantity();
            int targetStock = averageDaily.multiply(new BigDecimal(coverDays))
                    .setScale(0, RoundingMode.CEILING)
                    .intValue();
            int suggestedQty = Math.max(0, targetStock - currentStock);

            Integer daysUntilStockout = null;
            if (averageDaily.compareTo(BigDecimal.ZERO) > 0) {
                daysUntilStockout = new BigDecimal(currentStock)
                        .divide(averageDaily, 0, RoundingMode.DOWN)
                        .intValue();
            }

            String urgency = urgency(currentStock, suggestedQty, daysUntilStockout, leadTimeDays);

            SupplierPick supplierPick = pickSupplier(purchaseBatches);
            BigDecimal lastUnitCost = supplierPick.lastUnitCost != null
                    ? supplierPick.lastUnitCost
                    : product.getCostPrice();
            BigDecimal orderCost = lastUnitCost != null
                    ? lastUnitCost.multiply(BigDecimal.valueOf(suggestedQty)).setScale(2, RoundingMode.HALF_UP)
                    : null;

            ReorderSuggestionItemDTO item = new ReorderSuggestionItemDTO();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setSku(product.getSku());
            item.setCategory(product.getCategory() != null ? product.getCategory().getName() : null);
            item.setCurrentStock(currentStock);
            item.setStaticReorderLevel(product.getReorderLevel());
            item.setUnitsSold(unitsSold);
            item.setAverageDailyUnitsSold(averageDaily.setScale(2, RoundingMode.HALF_UP));
            item.setLeadTimeDays(leadTimeDays);
            item.setSafetyDays(safetyDays);
            item.setCoverDays(coverDays);
            item.setTargetStock(targetStock);
            item.setSuggestedQty(suggestedQty);
            item.setEstimatedDaysUntilStockout(daysUntilStockout);
            item.setUrgency(urgency);
            item.setSuggestedSupplierId(supplierPick.supplierId);
            item.setSuggestedSupplierName(supplierPick.supplierName);
            item.setSupplierReason(supplierPick.reason);
            item.setLastReceivedDate(supplierPick.lastReceivedDate);
            item.setPurchaseBatchCount(purchaseBatches.size());
            item.setTypicalReceiptQty(typicalReceiptQty(purchaseBatches));
            item.setLastUnitCost(lastUnitCost != null ? lastUnitCost.setScale(2, RoundingMode.HALF_UP) : null);
            item.setEstimatedOrderCost(orderCost);

            items.add(item);
            if (suggestedQty > 0 && "CRITICAL".equals(urgency)) {
                criticalCount++;
            }
            if (suggestedQty > 0 && supplierPick.supplierId == null) {
                missingSupplierCount++;
            }
            if (suggestedQty > 0 && orderCost != null) {
                estimatedOrderCost = estimatedOrderCost.add(orderCost);
            }
        }

        items.sort((a, b) -> {
            int urgencyCmp = Integer.compare(urgencyRank(a.getUrgency()), urgencyRank(b.getUrgency()));
            if (urgencyCmp != 0) {
                return urgencyCmp;
            }
            int daysA = a.getEstimatedDaysUntilStockout() != null ? a.getEstimatedDaysUntilStockout() : Integer.MAX_VALUE;
            int daysB = b.getEstimatedDaysUntilStockout() != null ? b.getEstimatedDaysUntilStockout() : Integer.MAX_VALUE;
            int daysCmp = Integer.compare(daysA, daysB);
            if (daysCmp != 0) {
                return daysCmp;
            }
            return a.getProductName().compareToIgnoreCase(b.getProductName());
        });

        ReorderSuggestionsSummaryDTO result = new ReorderSuggestionsSummaryDTO();
        result.setPeriodStart(periodStart);
        result.setPeriodEnd(periodEnd);
        result.setLookbackDays(lookbackDays);
        result.setTotalProducts(products.size());
        result.setProductsWithSales(productsWithSales);
        result.setProductsNeedingReorder((int) items.stream().filter(i -> i.getSuggestedQty() > 0).count());
        result.setCriticalCount(criticalCount);
        result.setMissingSupplierCount(missingSupplierCount);
        result.setEstimatedOrderCost(estimatedOrderCost.setScale(2, RoundingMode.HALF_UP));
        result.setItems(items);
        if (sales.isEmpty()) {
            result.setMessage(NOT_ENOUGH_SALES);
        } else if (items.stream().noneMatch(i -> i.getSuggestedQty() > 0)) {
            result.setMessage(NO_REORDER_NEEDED);
        }
        return result;
    }

    private static List<InventoryBatch> purchaseBatches(List<InventoryBatch> batches) {
        List<InventoryBatch> purchases = new ArrayList<>();
        for (InventoryBatch batch : batches) {
            if (batch.getSupplier() != null) {
                purchases.add(batch);
            }
        }
        purchases.sort(Comparator.comparing(InventoryBatch::getReceivedDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return purchases;
    }

    private static int estimateLeadTimeDays(List<InventoryBatch> purchaseBatches) {
        if (purchaseBatches.size() < 2) {
            return DEFAULT_LEAD_TIME_DAYS;
        }
        long gapSum = 0;
        int gapCount = 0;
        for (int i = 1; i < purchaseBatches.size(); i++) {
            LocalDate prev = purchaseBatches.get(i - 1).getReceivedDate();
            LocalDate next = purchaseBatches.get(i).getReceivedDate();
            if (prev == null || next == null || next.isBefore(prev)) {
                continue;
            }
            long gap = ChronoUnit.DAYS.between(prev, next);
            if (gap > 0) {
                gapSum += gap;
                gapCount++;
            }
        }
        if (gapCount == 0) {
            return DEFAULT_LEAD_TIME_DAYS;
        }
        int average = (int) Math.round((double) gapSum / gapCount);
        return Math.min(MAX_LEAD_TIME_DAYS, Math.max(MIN_LEAD_TIME_DAYS, average));
    }

    private static Integer typicalReceiptQty(List<InventoryBatch> purchaseBatches) {
        if (purchaseBatches.isEmpty()) {
            return null;
        }
        int total = 0;
        for (InventoryBatch batch : purchaseBatches) {
            total += batch.getQuantityReceived();
        }
        return (int) Math.round((double) total / purchaseBatches.size());
    }

    private static String urgency(int currentStock, int suggestedQty, Integer daysUntilStockout, int leadTimeDays) {
        if (currentStock <= 0 || (daysUntilStockout != null && daysUntilStockout <= leadTimeDays)) {
            return "CRITICAL";
        }
        if (suggestedQty > 0) {
            return "REORDER";
        }
        return "OK";
    }

    private static int urgencyRank(String urgency) {
        if ("CRITICAL".equals(urgency)) {
            return 0;
        }
        if ("REORDER".equals(urgency)) {
            return 1;
        }
        return 2;
    }

    private static SupplierPick pickSupplier(List<InventoryBatch> purchaseBatches) {
        SupplierPick pick = new SupplierPick();
        if (purchaseBatches.isEmpty()) {
            pick.reason = "No purchase batches with a supplier — record Receive Stock against a supplier.";
            return pick;
        }

        Map<Long, Integer> qtyBySupplier = new HashMap<>();
        Map<Long, LocalDate> lastDateBySupplier = new HashMap<>();
        Map<Long, BigDecimal> lastCostBySupplier = new HashMap<>();
        Map<Long, String> nameBySupplier = new HashMap<>();

        for (InventoryBatch batch : purchaseBatches) {
            Supplier supplier = batch.getSupplier();
            Long id = supplier.getId();
            qtyBySupplier.merge(id, batch.getQuantityReceived(), Integer::sum);
            nameBySupplier.put(id, supplier.getName());
            LocalDate received = batch.getReceivedDate();
            LocalDate previous = lastDateBySupplier.get(id);
            if (received != null && (previous == null || received.isAfter(previous))) {
                lastDateBySupplier.put(id, received);
                if (batch.getPurchasePrice() != null) {
                    lastCostBySupplier.put(id, batch.getPurchasePrice());
                }
            }
        }

        Long bestId = qtyBySupplier.entrySet().stream()
                .max(Comparator
                        .comparingInt((Map.Entry<Long, Integer> e) -> e.getValue())
                        .thenComparing(e -> lastDateBySupplier.getOrDefault(e.getKey(), LocalDate.MIN)))
                .map(Map.Entry::getKey)
                .orElse(null);

        pick.supplierId = bestId;
        pick.supplierName = nameBySupplier.get(bestId);
        pick.lastReceivedDate = lastDateBySupplier.get(bestId);
        pick.lastUnitCost = lastCostBySupplier.get(bestId);
        int units = qtyBySupplier.getOrDefault(bestId, 0);
        pick.reason = "Highest volume in purchase history (" + units + " units received"
                + (pick.lastReceivedDate != null ? ", last on " + pick.lastReceivedDate : "")
                + ").";
        return pick;
    }

    private static class SupplierPick {
        Long supplierId;
        String supplierName;
        String reason;
        LocalDate lastReceivedDate;
        BigDecimal lastUnitCost;
    }
}
