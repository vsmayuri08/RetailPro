package com.retailsystem.ai;

import com.retailsystem.dto.DemandForecastItemDTO;
import com.retailsystem.dto.DemandForecastSummaryDTO;
import com.retailsystem.dto.InventoryInsightItemDTO;
import com.retailsystem.dto.InventoryInsightsSummaryDTO;
import com.retailsystem.entity.Product;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.service.DemandForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory insight numbers are computed here from Product + sales history
 * (via DemandForecastService). Gemini only writes a short narrative.
 */
@Service
public class AiInventoryInsightsService {

    private static final int FORECAST_DAYS = 7;
    private static final int STOCKOUT_WARN_DAYS = 7;
    private static final int OVERSTOCK_DAYS = 60;
    private static final int NEAR_EXPIRY_DAYS = 14;

    @Autowired
    private DemandForecastService demandForecastService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private AiPromptBuilder aiPromptBuilder;

    @Transactional(readOnly = true)
    public InventoryInsightsSummaryDTO generate(Long branchId) {
        DemandForecastSummaryDTO forecast = demandForecastService.generateForecast(
                branchId, null, null, FORECAST_DAYS);
        Map<Long, Product> products = productRepository.findByBranchIdAndActiveTrue(branchId).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<DemandForecastItemDTO> withSales = forecast.getItems().stream()
                .filter(DemandForecastItemDTO::isHasSales)
                .sorted(Comparator.comparing(DemandForecastItemDTO::getUnitsSold).reversed())
                .collect(Collectors.toList());
        int n = withSales.size();
        int fastCut = n == 0 ? 0 : Math.max(1, n / 5);
        int slowStart = n == 0 ? 0 : n - fastCut;

        List<InventoryInsightItemDTO> items = new ArrayList<>();
        int stockoutCount = 0;
        int overstockCount = 0;
        int deadStockCount = 0;
        int nearExpiryCount = 0;

        for (DemandForecastItemDTO row : forecast.getItems()) {
            Product product = products.get(row.getProductId());
            LocalDate expiry = product != null ? product.getExpiryDate() : null;
            LocalDate today = LocalDate.now();
            boolean expired = expiry != null && expiry.isBefore(today);
            boolean nearExpiry = expiry != null && !expired && !expiry.isAfter(today.plusDays(NEAR_EXPIRY_DAYS));
            boolean dead = !row.isHasSales() && row.getCurrentStock() > 0;
            boolean stockout = row.isStockoutRisk()
                    || (row.getEstimatedDaysUntilStockout() != null
                    && row.getEstimatedDaysUntilStockout() <= STOCKOUT_WARN_DAYS
                    && row.isHasSales());
            boolean overstock = row.getEstimatedDaysUntilStockout() != null
                    && row.getEstimatedDaysUntilStockout() > OVERSTOCK_DAYS;
            boolean unusual = "RISING".equals(row.getTrend()) || "FALLING".equals(row.getTrend());

            int salesRank = withSales.indexOf(row);
            boolean fast = row.isHasSales() && salesRank >= 0 && salesRank < fastCut;
            boolean slow = row.isHasSales() && salesRank >= slowStart && n >= 2;

            String type;
            String risk;
            if (expired || stockout) {
                type = expired ? "EXPIRED" : "STOCKOUT";
                risk = "HIGH";
                stockoutCount++;
            } else if (nearExpiry) {
                type = "NEAR_EXPIRY";
                risk = "HIGH";
                nearExpiryCount++;
            } else if (dead) {
                type = "DEAD_STOCK";
                risk = "MEDIUM";
                deadStockCount++;
            } else if (overstock) {
                type = "OVERSTOCK";
                risk = "MEDIUM";
                overstockCount++;
            } else if (unusual) {
                type = "UNUSUAL_SALES";
                risk = "MEDIUM";
            } else if (fast) {
                type = "FAST_MOVING";
                risk = "LOW";
            } else if (slow) {
                type = "SLOW_MOVING";
                risk = "LOW";
            } else {
                continue;
            }

            InventoryInsightItemDTO item = new InventoryInsightItemDTO();
            item.setProductId(row.getProductId());
            item.setProductName(row.getProductName());
            item.setSku(row.getSku());
            item.setCurrentStock(row.getCurrentStock());
            item.setAverageDailySales(row.getAverageDailyUnitsSold());
            item.setPredictedDemand(row.getForecastedDemand());
            item.setDaysOfStockRemaining(row.getEstimatedDaysUntilStockout());
            item.setRiskLevel(risk);
            item.setInsightType(type);
            item.setExpiryDate(expiry);
            item.setHasSales(row.isHasSales());
            item.setTrend(row.getTrend());
            item.setHeadline(headline(item, type));
            item.setExplanation(item.getHeadline());
            items.add(item);
        }

        items.sort(Comparator
                .comparingInt((InventoryInsightItemDTO i) -> riskRank(i.getRiskLevel()))
                .thenComparingInt(i -> typeRank(i.getInsightType()))
                .thenComparing(i -> i.getDaysOfStockRemaining() == null ? Integer.MAX_VALUE : i.getDaysOfStockRemaining()));

        InventoryInsightsSummaryDTO summary = new InventoryInsightsSummaryDTO();
        summary.setStockoutCount(stockoutCount);
        summary.setOverstockCount(overstockCount);
        summary.setDeadStockCount(deadStockCount);
        summary.setNearExpiryCount(nearExpiryCount);
        summary.setItems(items);
        summary.setNarrative(narrative(summary, items));
        return summary;
    }

    public void attachGeminiNarrative(InventoryInsightsSummaryDTO summary) {
        if (summary.getItems().isEmpty()) {
            summary.setNarrative(AiPromptBuilder.NOT_ENOUGH);
            return;
        }
        StringBuilder facts = new StringBuilder();
        facts.append("stockoutCount=").append(summary.getStockoutCount()).append('\n');
        facts.append("overstockCount=").append(summary.getOverstockCount()).append('\n');
        facts.append("deadStockCount=").append(summary.getDeadStockCount()).append('\n');
        facts.append("nearExpiryCount=").append(summary.getNearExpiryCount()).append('\n');
        facts.append("HEADLINES:\n");
        summary.getItems().stream().limit(12).forEach(i ->
                facts.append("- ").append(i.getHeadline())
                        .append(" | stock=").append(i.getCurrentStock())
                        .append(" avgDaily=").append(i.getAverageDailySales())
                        .append(" predicted7d=").append(i.getPredictedDemand())
                        .append(" daysLeft=").append(i.getDaysOfStockRemaining())
                        .append('\n'));
        String text = geminiService.generate(aiPromptBuilder.insightsNarrativePrompt(facts.toString()));
        if (text != null && !text.isBlank() && !AiPromptBuilder.NOT_ENOUGH.equals(text)) {
            summary.setNarrative(text);
        }
    }

    private static String headline(InventoryInsightItemDTO item, String type) {
        return switch (type) {
            case "EXPIRED" -> item.getProductName() + " is expired (" + item.getExpiryDate() + ") with "
                    + item.getCurrentStock() + " units still on hand.";
            case "STOCKOUT" -> {
                Integer days = item.getDaysOfStockRemaining();
                yield days == null
                        ? item.getProductName() + " is at stockout risk (stock " + item.getCurrentStock() + ")."
                        : item.getProductName() + " may run out within " + days + " days.";
            }
            case "NEAR_EXPIRY" -> item.getProductName() + " is near expiry (" + item.getExpiryDate()
                    + "), stock " + item.getCurrentStock() + ".";
            case "DEAD_STOCK" -> item.getProductName() + " has " + item.getCurrentStock()
                    + " units and no sales in the last 90 days (dead stock).";
            case "OVERSTOCK" -> item.getProductName() + " has about " + item.getDaysOfStockRemaining()
                    + " days of stock — overstock risk.";
            case "UNUSUAL_SALES" -> item.getProductName() + " sales trend is " + item.getTrend()
                    + " (" + item.getAverageDailySales() + " units/day).";
            case "FAST_MOVING" -> item.getProductName() + " is fast-moving (predicted 7-day demand "
                    + item.getPredictedDemand() + ").";
            default -> item.getProductName() + " is slow-moving (" + item.getAverageDailySales() + " units/day).";
        };
    }

    private static String narrative(InventoryInsightsSummaryDTO summary, List<InventoryInsightItemDTO> items) {
        if (items.isEmpty()) {
            return AiPromptBuilder.NOT_ENOUGH;
        }
        return "Backend counted " + summary.getStockoutCount() + " stockout-risk SKUs, "
                + summary.getNearExpiryCount() + " near expiry, "
                + summary.getDeadStockCount() + " dead stock, and "
                + summary.getOverstockCount() + " overstock. Review the table — figures are from sales velocity and current stock, not guesses.";
    }

    private static int riskRank(String risk) {
        if ("HIGH".equals(risk)) {
            return 0;
        }
        if ("MEDIUM".equals(risk)) {
            return 1;
        }
        return 2;
    }

    private static int typeRank(String type) {
        return switch (type) {
            case "EXPIRED", "STOCKOUT" -> 0;
            case "NEAR_EXPIRY" -> 1;
            case "DEAD_STOCK" -> 2;
            case "OVERSTOCK" -> 3;
            case "UNUSUAL_SALES" -> 4;
            default -> 5;
        };
    }
}
