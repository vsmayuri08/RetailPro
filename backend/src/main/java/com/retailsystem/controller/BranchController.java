package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.BranchDTO;
import com.retailsystem.dto.BranchRequest;
import com.retailsystem.dto.OrgStatsDTO;
import com.retailsystem.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin endpoints for branch management.
 * Locked to ROLE_SUPER_ADMIN by the /api/admin/** rule in SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/branches")
public class BranchController {

    @Autowired
    private BranchService branchService;

    @PostMapping
    public ResponseEntity<ApiResponse<BranchDTO>> createBranch(@Valid @RequestBody BranchRequest request) {
        BranchDTO branch = branchService.createBranch(request);
        return ResponseEntity.ok(ApiResponse.success("Branch created successfully", branch));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> updateBranch(@PathVariable Long id, @Valid @RequestBody BranchRequest request) {
        BranchDTO branch = branchService.updateBranch(id, request);
        return ResponseEntity.ok(ApiResponse.success("Branch updated successfully", branch));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<BranchDTO>> activateBranch(@PathVariable Long id) {
        BranchDTO branch = branchService.setBranchStatus(id, true);
        return ResponseEntity.ok(ApiResponse.success("Branch activated", branch));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<BranchDTO>> deactivateBranch(@PathVariable Long id) {
        BranchDTO branch = branchService.setBranchStatus(id, false);
        return ResponseEntity.ok(ApiResponse.success("Branch deactivated", branch));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchDTO>> getBranch(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(branchService.getBranch(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchDTO>>> getAllBranches() {
        return ResponseEntity.ok(ApiResponse.success(branchService.getAllBranches()));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OrgStatsDTO>> getOrgStats() {
        return ResponseEntity.ok(ApiResponse.success(branchService.getOrgStats()));
    }
}
