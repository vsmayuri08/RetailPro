package com.retailsystem.ai;

import com.retailsystem.dto.AbcAnalysisItemDTO;
import com.retailsystem.dto.AbcAnalysisSummaryDTO;
import com.retailsystem.dto.BranchSalesSummaryDTO;
import com.retailsystem.dto.DemandForecastItemDTO;
import com.retailsystem.dto.DemandForecastSummaryDTO;
import com.retailsystem.dto.OrgStatsDTO;
import com.retailsystem.dto.ReorderSuggestionItemDTO;
import com.retailsystem.dto.ReorderSuggestionsSummaryDTO;
import com.retailsystem.dto.SalesTrendPointDTO;
import com.retailsystem.dto.SalesTrendSummaryDTO;
import com.retailsystem.entity.Product;
import com.retailsystem.entity.Sale;
import com.retailsystem.repository.ProductRepository;
import com.retailsystem.repository.SaleRepository;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.AbcAnalysisService;
import com.retailsystem.service.BranchService;
import com.retailsystem.service.DemandForecastService;
import com.retailsystem.service.ReorderSuggestionsService;
import com.retailsystem.service.SalesTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads live RetailPro rows and formats them as a FACTS block for Gemini.
 * Gemini never sees the database — only this snapshot.
 */
@Service
public class AiContextService {

    private static final int LOOKBACK_DAYS = 90;
    private static final int LINE_CAP = 15;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BranchService branchService;

    @Autowired
    private AbcAnalysisService abcAnalysisService;

    @Autowired
    private DemandForecastService demandForecastService;

    @Autowired
    private ReorderSuggestionsService reorderSuggestionsService;

    @Autowired
    private SalesTrendService salesTrendService;

    @Transactional(readOnly = true)
    public String buildFacts(CustomUserDetails principal) {
        StringBuilder facts = new StringBuilder();
        LocalDate today = LocalDate.now();
        facts.append("TODAY=").append(today).append('\n');
        facts.append("ROLE=").append(principal.getRole()).append('\n');

        boolean admin = "SUPER_ADMIN".equals(principal.getRole());
        Long branchId = principal.getBranchId();

        if (admin) {
            facts.append("SCOPE=ORGANIZATION\n");
            appendOrg(facts);
            appendSalesTrend(facts, null);
            for (var branch : branchService.getAllBranches()) {
                if (branch.getId() == null || !"ACTIVE".equals(branch.getStatus())) {
                    continue;
                }
                facts.append("\n--- BRANCH ").append(branch.getBranchName())
                        .append(" id=").append(branch.getId()).append(" ---\n");
                appendBranchSales(facts, branch.getId(), today);
                appendProducts(facts, branch.getId(), today);
                appendAbc(facts, branch.getId());
                appendForecast(facts, branch.getId());
                appendReorder(facts, branch.getId());
            }
        } else if (branchId != null) {
            facts.append("SCOPE=BRANCH_ID=").append(branchId).append('\n');
            appendBranchSales(facts, branchId, today);
            appendProducts(facts, branchId, today);
            appendAbc(facts, branchId);
            appendForecast(facts, branchId);
            appendReorder(facts, branchId);
            appendSalesTrend(facts, branchId);
        } else {
            facts.append("SCOPE=NONE\n");
            facts.append("NOTE=This account is not assigned to a branch.\n");
        }
        return facts.toString();
    }

    private void appendOrg(StringBuilder facts) {
        OrgStatsDTO org = branchService.getOrgStats();
        facts.append("ORG_BRANCHES=").append(org.getTotalBranches())
                .append(" active=").append(org.getActiveBranches()).append('\n');
        facts.append("ORG_SALES_TODAY count=").append(org.getTotalSalesToday())
                .append(" revenueRs=").append(money(org.getTotalRevenueToday())).append('\n');
        facts.append("ORG_SALES_MONTH count=").append(org.getTotalSalesThisMonth())
                .append(" revenueRs=").append(money(org.getTotalRevenueThisMonth())).append('\n');
        facts.append("BRANCH_PERFORMANCE (name|salesToday|revenueTodayRs|salesMonth|revenueMonthRs):\n");
        List<BranchSalesSummaryDTO> rows = org.getBranchSales() == null ? List.of() : org.getBranchSales();
        rows.stream()
                .sorted(Comparator.comparing(BranchSalesSummaryDTO::getRevenueThisMonth).reversed())
                .limit(LINE_CAP)
                .forEach(row -> facts.append("- ")
                        .append(row.getBranchName())
                        .append('|').append(row.getSalesCountToday())
                        .append('|').append(money(row.getRevenueToday()))
                        .append('|').append(row.getSalesCountThisMonth())
                        .append('|').append(money(row.getRevenueThisMonth()))
                        .append('\n'));
        if (rows.isEmpty()) {
            facts.append("- (none)\n");
        }
    }

    private void appendBranchSales(StringBuilder facts, Long branchId, LocalDate today) {
        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime startTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime startMonth = today.withDayOfMonth(1).atStartOfDay();
        List<Sale> month = saleRepository.findByBranch_IdAndCreatedAtBetween(branchId, startMonth, startTomorrow);
        List<Sale> todaySales = month.stream()
                .filter(s -> !s.getCreatedAt().isBefore(startToday))
                .collect(Collectors.toList());
        facts.append("SALES_TODAY count=").append(todaySales.size())
                .append(" revenueRs=").append(money(sum(todaySales))).append('\n');
        facts.append("SALES_MONTH count=").append(month.size())
                .append(" revenueRs=").append(money(sum(month))).append('\n');
    }

    private void appendProducts(StringBuilder facts, Long branchId, LocalDate today) {
        List<Product> products = productRepository.findByBranchIdAndActiveTrue(branchId);
        facts.append("ACTIVE_PRODUCTS=").append(products.size()).append('\n');
        List<Product> byStock = products.stream()
                .sorted(Comparator.comparingInt(Product::getQuantity).thenComparing(Product::getName))
                .collect(Collectors.toList());
        facts.append("CURRENT_STOCK all active SKUs sorted lowest stock first (name|sku|stock|reorderLevel):\n");
        if (byStock.isEmpty()) {
            facts.append("- (none)\n");
        } else {
            byStock.stream().limit(40).forEach(p -> facts.append("- ").append(p.getName())
                    .append('|').append(p.getSku())
                    .append('|').append(p.getQuantity())
                    .append('|').append(p.getReorderLevel()).append('\n'));
            Product lowest = byStock.get(0);
            Product highest = byStock.get(byStock.size() - 1);
            facts.append("LOWEST_STOCK name=").append(lowest.getName())
                    .append(" sku=").append(lowest.getSku())
                    .append(" stock=").append(lowest.getQuantity()).append('\n');
            facts.append("HIGHEST_STOCK name=").append(highest.getName())
                    .append(" sku=").append(highest.getSku())
                    .append(" stock=").append(highest.getQuantity()).append('\n');
        }
        facts.append("NEAR_EXPIRY_OR_EXPIRED (name|sku|stock|expiry|flag):\n");
        List<Product> expiry = products.stream()
                .filter(p -> p.getExpiryDate() != null)
                .filter(p -> !p.getExpiryDate().isAfter(today.plusDays(14)))
                .sorted(Comparator.comparing(Product::getExpiryDate))
                .limit(LINE_CAP)
                .collect(Collectors.toList());
        if (expiry.isEmpty()) {
            facts.append("- (none)\n");
        } else {
            for (Product p : expiry) {
                String flag = p.getExpiryDate().isBefore(today) ? "EXPIRED" : "NEAR_EXPIRY";
                facts.append("- ").append(p.getName()).append('|').append(p.getSku())
                        .append('|').append(p.getQuantity())
                        .append('|').append(p.getExpiryDate())
                        .append('|').append(flag).append('\n');
            }
        }
        facts.append("LOW_VS_CATALOG_REORDER_LEVEL (name|sku|stock|reorderLevel) — catalog field only, not velocity:\n");
        List<Product> low = products.stream()
                .filter(p -> p.getQuantity() <= p.getReorderLevel())
                .limit(LINE_CAP)
                .collect(Collectors.toList());
        if (low.isEmpty()) {
            facts.append("- (none)\n");
        } else {
            for (Product p : low) {
                facts.append("- ").append(p.getName()).append('|').append(p.getSku())
                        .append('|').append(p.getQuantity())
                        .append('|').append(p.getReorderLevel()).append('\n');
            }
        }
    }

    private void appendAbc(StringBuilder facts, Long branchId) {
        AbcAnalysisSummaryDTO abc = abcAnalysisService.generateAnalysis(branchId, null, null);
        facts.append("ABC_PERIOD=").append(abc.getPeriodStart()).append(" to ").append(abc.getPeriodEnd())
                .append(" totalRevenueRs=").append(money(abc.getTotalRevenue())).append('\n');
        facts.append("TOP_SELLERS_BY_REVENUE (rank|name|sku|units|revenueRs|class):\n");
        List<AbcAnalysisItemDTO> withSales = abc.getItems().stream()
                .filter(AbcAnalysisItemDTO::isHasSales)
                .limit(LINE_CAP)
                .collect(Collectors.toList());
        if (withSales.isEmpty()) {
            facts.append("- (none)\n");
        } else {
            for (AbcAnalysisItemDTO item : withSales) {
                facts.append("- ").append(item.getRank()).append('|').append(item.getProductName())
                        .append('|').append(item.getSku())
                        .append('|').append(item.getUnitsSold())
                        .append('|').append(money(item.getRevenue()))
                        .append('|').append(item.getAbcClass()).append('\n');
            }
        }
        facts.append("SLOW_OR_DEAD_STOCK class C with stock (name|sku|unitsSold|stock|stockValueRs):\n");
        List<AbcAnalysisItemDTO> dead = abc.getItems().stream()
                .filter(i -> "C".equals(i.getAbcClass()) && i.getCurrentStock() > 0)
                .sorted(Comparator.comparing(AbcAnalysisItemDTO::getStockValue).reversed())
                .limit(LINE_CAP)
                .collect(Collectors.toList());
        if (dead.isEmpty()) {
            facts.append("- (none)\n");
        } else {
            for (AbcAnalysisItemDTO item : dead) {
                facts.append("- ").append(item.getProductName()).append('|').append(item.getSku())
                        .append('|').append(item.getUnitsSold())
                        .append('|').append(item.getCurrentStock())
                        .append('|').append(money(item.getStockValue())).append('\n');
            }
        }
    }

    private void appendForecast(StringBuilder facts, Long branchId) {
        DemandForecastSummaryDTO forecast = demandForecastService.generateForecast(branchId, null, null, 7);
        facts.append("DEMAND_LOOKBACK_DAYS=").append(forecast.getLookbackDays())
                .append(" forecastDays=").append(forecast.getForecastDays()).append('\n');
        facts.append("STOCKOUT_RISK (name|sku|stock|avgDaily|daysLeft|trend):\n");
        List<DemandForecastItemDTO> risk = forecast.getItems().stream()
                .filter(DemandForecastItemDTO::isStockoutRisk)
                .limit(LINE_CAP)
                .collect(Collectors.toList());
        if (risk.isEmpty()) {
            facts.append("- (none)\n");
        } else {
            for (DemandForecastItemDTO item : risk) {
                facts.append("- ").append(item.getProductName()).append('|').append(item.getSku())
                        .append('|').append(item.getCurrentStock())
                        .append('|').append(item.getAverageDailyUnitsSold())
                        .append('|').append(item.getEstimatedDaysUntilStockout())
                        .append('|').append(item.getTrend()).append('\n');
            }
        }
        facts.append("UNUSUAL_TREND RISING/FALLING (name|sku|units|trend):\n");
        List<DemandForecastItemDTO> unusual = forecast.getItems().stream()
                .filter(i -> "RISING".equals(i.getTrend()) || "FALLING".equals(i.getTrend()))
                .limit(LINE_CAP)
                .collect(Collectors.toList());
        if (unusual.isEmpty()) {
            facts.append("- (none)\n");
        } else {
            for (DemandForecastItemDTO item : unusual) {
                facts.append("- ").append(item.getProductName()).append('|').append(item.getSku())
                        .append('|').append(item.getUnitsSold())
                        .append('|').append(item.getTrend()).append('\n');
            }
        }
    }

    private void appendReorder(StringBuilder facts, Long branchId) {
        ReorderSuggestionsSummaryDTO reorder = reorderSuggestionsService.generateSuggestions(branchId, null, null);
        facts.append("VELOCITY_REORDER needCount=").append(reorder.getProductsNeedingReorder()).append('\n');
        facts.append("REORDER_SUGGESTIONS (name|sku|stock|orderQty|supplier|urgency):\n");
        List<ReorderSuggestionItemDTO> need = reorder.getItems().stream()
                .filter(i -> i.getSuggestedQty() > 0)
                .limit(LINE_CAP)
                .collect(Collectors.toList());
        if (need.isEmpty()) {
            facts.append("- (none)\n");
        } else {
            for (ReorderSuggestionItemDTO item : need) {
                facts.append("- ").append(item.getProductName()).append('|').append(item.getSku())
                        .append('|').append(item.getCurrentStock())
                        .append('|').append(item.getSuggestedQty())
                        .append('|').append(item.getSuggestedSupplierName() == null ? "unknown" : item.getSuggestedSupplierName())
                        .append('|').append(item.getUrgency()).append('\n');
            }
        }
    }

    private void appendSalesTrend(StringBuilder facts, Long branchId) {
        SalesTrendSummaryDTO trend = salesTrendService.generate(branchId);
        facts.append("SALES_TREND method=").append(trend.getMethod()).append('\n');
        facts.append("PREDICTED_NEXT_MONTH_REVENUE_RS=").append(money(trend.getPredictedNextMonthRevenue())).append('\n');
        facts.append("EXPECTED_GROWTH_PCT=").append(trend.getExpectedGrowthPercent() == null ? "n/a" : trend.getExpectedGrowthPercent()).append('\n');
        facts.append("MONTHLY_REVENUE (label|kind|revenueRs):\n");
        for (SalesTrendPointDTO point : trend.getPoints()) {
            facts.append("- ").append(point.getLabel())
                    .append('|').append(point.getKind())
                    .append('|').append(money(point.getRevenue())).append('\n');
        }
    }

    private static BigDecimal sum(List<Sale> sales) {
        return sales.stream().map(Sale::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
