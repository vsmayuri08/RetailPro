package com.retailsystem.controller;

import com.retailsystem.dto.ActivityLogDTO;
import com.retailsystem.dto.ApiResponse;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager/activity-logs")
public class ManagerActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityLogDTO>>> list(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        return ResponseEntity.ok(ApiResponse.success(activityLogService.listForBranch(branchId)));
    }
}
