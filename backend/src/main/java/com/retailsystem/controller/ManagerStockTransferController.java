package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.StockTransferDTO;
import com.retailsystem.dto.StockTransferRequest;
import com.retailsystem.dto.TransferDecisionRequest;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.StockTransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Branch Manager -> "Approve stock transfers where permitted". */
@RestController
@RequestMapping("/api/manager/stock-transfers")
public class ManagerStockTransferController {

    @Autowired
    private StockTransferService stockTransferService;

    @PostMapping
    public ResponseEntity<ApiResponse<StockTransferDTO>> createRequest(@AuthenticationPrincipal CustomUserDetails principal,
                                                                         @Valid @RequestBody StockTransferRequest request) {
        StockTransferDTO transfer = stockTransferService.createRequest(
                ManagerProductController.requireBranchId(principal), principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Transfer request submitted", transfer));
    }

    /** Requests waiting on THIS branch to decide (this branch is the source). */
    @GetMapping("/incoming")
    public ResponseEntity<ApiResponse<List<StockTransferDTO>>> getIncoming(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                stockTransferService.getIncoming(ManagerProductController.requireBranchId(principal))));
    }

    /** Requests THIS branch raised (this branch is the destination). */
    @GetMapping("/outgoing")
    public ResponseEntity<ApiResponse<List<StockTransferDTO>>> getOutgoing(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                stockTransferService.getOutgoing(ManagerProductController.requireBranchId(principal))));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<StockTransferDTO>> approve(@AuthenticationPrincipal CustomUserDetails principal,
                                                                   @PathVariable Long id,
                                                                   @RequestBody(required = false) TransferDecisionRequest request) {
        StockTransferDTO transfer = stockTransferService.decide(
                ManagerProductController.requireBranchId(principal), principal.getId(), id, true, request);
        return ResponseEntity.ok(ApiResponse.success("Transfer approved", transfer));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<StockTransferDTO>> reject(@AuthenticationPrincipal CustomUserDetails principal,
                                                                  @PathVariable Long id,
                                                                  @RequestBody(required = false) TransferDecisionRequest request) {
        StockTransferDTO transfer = stockTransferService.decide(
                ManagerProductController.requireBranchId(principal), principal.getId(), id, false, request);
        return ResponseEntity.ok(ApiResponse.success("Transfer rejected", transfer));
    }
}
