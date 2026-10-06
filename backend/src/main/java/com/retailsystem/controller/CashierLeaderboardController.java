package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CashierLeaderboardSummaryDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.CashierLeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cashier/leaderboard")
public class CashierLeaderboardController {

    @Autowired
    private CashierLeaderboardService cashierLeaderboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<CashierLeaderboardSummaryDTO>> thisMonth(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        return ResponseEntity.ok(ApiResponse.success(cashierLeaderboardService.thisMonth(branchId)));
    }
}
