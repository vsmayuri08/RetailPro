package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CreateSaleRequest;
import com.retailsystem.dto.CreateSaleReturnRequest;
import com.retailsystem.dto.SaleDTO;
import com.retailsystem.dto.SaleReturnDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.ActivityLogService;
import com.retailsystem.service.SaleReturnService;
import com.retailsystem.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * Module 9 -> the POS billing endpoints. Checkout is scoped to the
 * cashier's own branch (their JWT branchId), same enforcement pattern as
 * every other /api/manager|cashier/** controller in this codebase.
 * Returns live here so they reuse /api/cashier/** — no SecurityConfig change.
 */
@RestController
@RequestMapping("/api/cashier/sales")
public class CashierSaleController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private SaleReturnService saleReturnService;

    @Autowired
    private ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<SaleDTO>> checkout(@AuthenticationPrincipal CustomUserDetails principal,
                                                           @Valid @RequestBody CreateSaleRequest request) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        SaleDTO sale = saleService.checkout(branchId, principal.getId(), request);
        activityLogService.record(principal, "SALE_COMPLETED", "Sale", String.valueOf(sale.getId()),
                sale.getInvoiceNumber() + " · Rs. " + sale.getTotalAmount());
        return ResponseEntity.ok(ApiResponse.success("Sale completed", sale));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleDTO>>> getRecentSales(@AuthenticationPrincipal CustomUserDetails principal) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        List<SaleDTO> sales = saleService.getRecentSalesForBranch(branchId);
        saleReturnService.annotateReturnedQuantities(sales);
        return ResponseEntity.ok(ApiResponse.success(sales));
    }

    @PostMapping("/{id}/returns")
    public ResponseEntity<ApiResponse<SaleReturnDTO>> processReturn(@AuthenticationPrincipal CustomUserDetails principal,
                                                                     @PathVariable Long id,
                                                                     @Valid @RequestBody CreateSaleReturnRequest request) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        SaleReturnDTO saleReturn = saleReturnService.processReturn(branchId, principal.getId(), id, request);
        activityLogService.record(principal, "SALE_RETURNED", "SaleReturn", String.valueOf(saleReturn.getId()),
                saleReturn.getInvoiceNumber() + " · refund Rs. " + saleReturn.getRefundAmount());
        return ResponseEntity.ok(ApiResponse.success("Return processed", saleReturn));
    }

    @GetMapping("/{id}/returns")
    public ResponseEntity<ApiResponse<List<SaleReturnDTO>>> getSaleReturns(@AuthenticationPrincipal CustomUserDetails principal,
                                                                            @PathVariable Long id) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        return ResponseEntity.ok(ApiResponse.success(saleReturnService.getReturnsForSale(id, branchId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleDTO>> getSale(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        SaleDTO sale = saleService.getSale(id, branchId);
        saleReturnService.annotateReturnedQuantities(Collections.singletonList(sale));
        return ResponseEntity.ok(ApiResponse.success(sale));
    }

    @GetMapping("/{id}/invoice.pdf")
    public ResponseEntity<byte[]> downloadInvoice(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @PathVariable Long id) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        SaleDTO sale = saleService.getSale(id, branchId);
        byte[] pdf = saleService.invoicePdf(id, branchId);
        return PublicReceiptController.pdfResponse(sale.getInvoiceNumber(), pdf);
    }
}
