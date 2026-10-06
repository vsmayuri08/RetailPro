package com.retailsystem.controller;

import com.retailsystem.dto.ActivityLogDTO;
import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CashierLeaderboardSummaryDTO;
import com.retailsystem.service.ActivityLogService;
import com.retailsystem.service.CashierLeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminActivityController {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private CashierLeaderboardService cashierLeaderboardService;

    @GetMapping("/activity-logs")
    public ResponseEntity<ApiResponse<List<ActivityLogDTO>>> activityLogs() {
        return ResponseEntity.ok(ApiResponse.success(activityLogService.listForAdmin()));
    }

    @GetMapping("/reports/cashier-leaderboard")
    public ResponseEntity<ApiResponse<CashierLeaderboardSummaryDTO>> cashierLeaderboard() {
        return ResponseEntity.ok(ApiResponse.success(cashierLeaderboardService.thisMonth(null)));
    }
}
