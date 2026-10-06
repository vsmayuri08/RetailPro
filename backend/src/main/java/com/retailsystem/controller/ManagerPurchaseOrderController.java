package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CreatePurchaseOrdersResultDTO;
import com.retailsystem.dto.PurchaseOrderDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.ActivityLogService;
import com.retailsystem.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager/purchase-orders")
public class ManagerPurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private ActivityLogService activityLogService;

    @PostMapping("/from-reorder")
    public ResponseEntity<ApiResponse<CreatePurchaseOrdersResultDTO>> createFromReorder(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        CreatePurchaseOrdersResultDTO result = purchaseOrderService.createFromReorder(branchId, principal.getId());
        activityLogService.record(principal, "PURCHASE_ORDER_CREATED", "PurchaseOrder",
                result.getOrders().stream().map(PurchaseOrderDTO::getPoNumber).reduce((a, b) -> a + "," + b).orElse(""),
                result.getNotice());
        return ResponseEntity.ok(ApiResponse.success(result.getNotice(), result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrderDTO>>> list(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                purchaseOrderService.list(ManagerProductController.requireBranchId(principal))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> get(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                purchaseOrderService.get(ManagerProductController.requireBranchId(principal), id)));
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> submit(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        PurchaseOrderDTO po = purchaseOrderService.submit(ManagerProductController.requireBranchId(principal), id);
        return ResponseEntity.ok(ApiResponse.success("Submitted for approval — not sent to the supplier", po));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> approve(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        PurchaseOrderDTO po = purchaseOrderService.approve(
                ManagerProductController.requireBranchId(principal), principal.getId(), id);
        activityLogService.record(principal, "PURCHASE_ORDER_APPROVED", "PurchaseOrder", po.getPoNumber(),
                "Approved internally. Not sent to " + po.getSupplierName());
        return ResponseEntity.ok(ApiResponse.success("Purchase order approved. It was not sent to the supplier.", po));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> cancel(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        PurchaseOrderDTO po = purchaseOrderService.cancel(ManagerProductController.requireBranchId(principal), id);
        activityLogService.record(principal, "PURCHASE_ORDER_CANCELLED", "PurchaseOrder", po.getPoNumber(), null);
        return ResponseEntity.ok(ApiResponse.success("Purchase order cancelled", po));
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<PurchaseOrderDTO>> receive(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        PurchaseOrderDTO po = purchaseOrderService.receive(ManagerProductController.requireBranchId(principal), id);
        activityLogService.record(principal, "PURCHASE_ORDER_RECEIVED", "PurchaseOrder", po.getPoNumber(),
                "Stock received against " + po.getPoNumber());
        return ResponseEntity.ok(ApiResponse.success("Stock received into inventory", po));
    }
}
