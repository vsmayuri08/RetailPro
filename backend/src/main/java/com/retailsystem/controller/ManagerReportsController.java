package com.retailsystem.controller;

import com.retailsystem.dto.AbcAnalysisSummaryDTO;
import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.DemandForecastSummaryDTO;
import com.retailsystem.dto.MarketBasketSummaryDTO;
import com.retailsystem.dto.ReorderSuggestionsSummaryDTO;
import com.retailsystem.dto.RfmSummaryDTO;
import com.retailsystem.dto.SalesTrendSummaryDTO;
import com.retailsystem.dto.CashierLeaderboardSummaryDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.AbcAnalysisService;
import com.retailsystem.service.CashierLeaderboardService;
import com.retailsystem.service.DemandForecastService;
import com.retailsystem.service.MarketBasketService;
import com.retailsystem.service.ReorderSuggestionsService;
import com.retailsystem.service.RfmAnalysisService;
import com.retailsystem.service.SalesTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Branch Manager -> analytical reports beyond the basic stats already on
 * the Sales page. ABC, demand forecast, market basket, and RFM all live
 * here so they share the existing /api/manager/** security rule — no extra
 * matcher needed. Reorder suggestions sit on the same controller for the
 * same reason.
 */
@RestController
@RequestMapping("/api/manager/reports")
public class ManagerReportsController {

    @Autowired
    private AbcAnalysisService abcAnalysisService;

    @Autowired
    private DemandForecastService demandForecastService;

    @Autowired
    private MarketBasketService marketBasketService;

    @Autowired
    private RfmAnalysisService rfmAnalysisService;

    @Autowired
    private ReorderSuggestionsService reorderSuggestionsService;

    @Autowired
    private CashierLeaderboardService cashierLeaderboardService;

    @Autowired
    private SalesTrendService salesTrendService;

    /**
     * ABC inventory analysis for the caller's branch. startDate/endDate are optional
     * (ISO yyyy-MM-dd) — defaults to the last 90 days if omitted.
     */
    @GetMapping("/abc-analysis")
    public ResponseEntity<ApiResponse<AbcAnalysisSummaryDTO>> getAbcAnalysis(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        AbcAnalysisSummaryDTO analysis = abcAnalysisService.generateAnalysis(branchId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }

    /**
     * Near-term demand forecast for the caller's branch. startDate/endDate are
     * optional (ISO yyyy-MM-dd) — defaults to the last 90 days if omitted.
     * forecastDays defaults to 7 (the projection horizon).
     */
    @GetMapping("/demand-forecast")
    public ResponseEntity<ApiResponse<DemandForecastSummaryDTO>> getDemandForecast(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer forecastDays) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        DemandForecastSummaryDTO forecast = demandForecastService.generateForecast(
                branchId, startDate, endDate, forecastDays);
        return ResponseEntity.ok(ApiResponse.success(forecast));
    }

    /**
     * Market basket (frequently-bought-together) for the caller's branch.
     * startDate/endDate are optional (ISO yyyy-MM-dd) — defaults to the last
     * 90 days if omitted. minSupport defaults to 2 co-occurrences.
     */
    @GetMapping("/market-basket")
    public ResponseEntity<ApiResponse<MarketBasketSummaryDTO>> getMarketBasket(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer minSupport) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        MarketBasketSummaryDTO analysis = marketBasketService.generateAnalysis(
                branchId, startDate, endDate, minSupport);
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }

    /**
     * RFM customer segmentation for the caller's branch. startDate/endDate are
     * optional (ISO yyyy-MM-dd) — defaults to the last 90 days if omitted.
     */
    @GetMapping("/rfm-segmentation")
    public ResponseEntity<ApiResponse<RfmSummaryDTO>> getRfmSegmentation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        RfmSummaryDTO analysis = rfmAnalysisService.generateAnalysis(branchId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analysis));
    }

    /**
     * Velocity-based reorder suggestions for the caller's branch. startDate/endDate
     * are optional (ISO yyyy-MM-dd) — defaults to the last 90 days if omitted.
     */
    @GetMapping("/reorder-suggestions")
    public ResponseEntity<ApiResponse<ReorderSuggestionsSummaryDTO>> getReorderSuggestions(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        ReorderSuggestionsSummaryDTO suggestions = reorderSuggestionsService.generateSuggestions(
                branchId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }

    @GetMapping("/cashier-leaderboard")
    public ResponseEntity<ApiResponse<CashierLeaderboardSummaryDTO>> getCashierLeaderboard(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        return ResponseEntity.ok(ApiResponse.success(cashierLeaderboardService.thisMonth(branchId)));
    }

    /**
     * Monthly revenue history plus next-month prediction from live Sale totals.
     * Super Admin (no branch) sees the organization; a manager sees their branch.
     */
    @GetMapping("/sales-trend")
    public ResponseEntity<ApiResponse<SalesTrendSummaryDTO>> getSalesTrend(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long branchId = "SUPER_ADMIN".equals(principal.getRole()) ? null : ManagerProductController.requireBranchId(principal);
        return ResponseEntity.ok(ApiResponse.success(salesTrendService.generate(branchId)));
    }
}
