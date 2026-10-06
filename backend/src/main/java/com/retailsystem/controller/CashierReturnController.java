package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.SaleReturnDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.SaleReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Branch-scoped return history for cashiers. Lives under /api/cashier/**
 * so the existing security rule covers it.
 */
@RestController
@RequestMapping("/api/cashier/returns")
public class CashierReturnController {

    @Autowired
    private SaleReturnService saleReturnService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleReturnDTO>>> getRecentReturns(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        return ResponseEntity.ok(ApiResponse.success(saleReturnService.getRecentReturnsForBranch(branchId)));
    }
}
