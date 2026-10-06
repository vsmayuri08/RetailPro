package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.ManagerDashboardStatsDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.ManagerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/dashboard")
public class ManagerDashboardController {

    @Autowired
    private ManagerDashboardService managerDashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ManagerDashboardStatsDTO>> getStats(@AuthenticationPrincipal CustomUserDetails principal) {
        ManagerDashboardStatsDTO stats = managerDashboardService.getStats(ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
