package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.ExpiryDiscountRuleDTO;
import com.retailsystem.dto.ExpiryDiscountRuleRequest;
import com.retailsystem.service.ExpiryDiscountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin -> Module 10's discount ladder. Locked to ROLE_SUPER_ADMIN by
 * the existing /api/admin/** rule.
 */
@RestController
@RequestMapping("/api/admin/expiry-discount-rules")
public class AdminExpiryDiscountController {

    @Autowired
    private ExpiryDiscountService expiryDiscountService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpiryDiscountRuleDTO>> createRule(@Valid @RequestBody ExpiryDiscountRuleRequest request) {
        ExpiryDiscountRuleDTO rule = expiryDiscountService.createRule(request);
        return ResponseEntity.ok(ApiResponse.success("Discount rule created", rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpiryDiscountRuleDTO>> updateRule(@PathVariable Long id, @Valid @RequestBody ExpiryDiscountRuleRequest request) {
        ExpiryDiscountRuleDTO rule = expiryDiscountService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.success("Discount rule updated", rule));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<ExpiryDiscountRuleDTO>> activateRule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Rule activated", expiryDiscountService.setActive(id, true)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<ExpiryDiscountRuleDTO>> deactivateRule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Rule deactivated", expiryDiscountService.setActive(id, false)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        expiryDiscountService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("Discount rule deleted", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpiryDiscountRuleDTO>>> getAllRules() {
        return ResponseEntity.ok(ApiResponse.success(expiryDiscountService.getAllRules()));
    }
}
