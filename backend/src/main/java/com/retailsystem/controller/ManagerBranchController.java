package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.BranchLiteDTO;
import com.retailsystem.entity.Branch;
import com.retailsystem.enums.BranchStatus;
import com.retailsystem.repository.BranchRepository;
import com.retailsystem.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-only, safe-fields-only branch list for Branch Managers — used to
 * pick "which branch am I requesting stock from" in the transfer form.
 * Does not expose staff counts or contact info (see BranchDTO for that,
 * which stays Super-Admin-only).
 */
@RestController
@RequestMapping("/api/manager/branches")
public class ManagerBranchController {

    @Autowired
    private BranchRepository branchRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchLiteDTO>>> getOtherActiveBranches(@AuthenticationPrincipal CustomUserDetails principal) {
        Long ownBranchId = principal.getBranchId();
        List<BranchLiteDTO> branches = branchRepository.findByStatus(BranchStatus.ACTIVE).stream()
                .filter(b -> ownBranchId == null || !b.getId().equals(ownBranchId))
                .map(BranchLiteDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(branches));
    }
}
