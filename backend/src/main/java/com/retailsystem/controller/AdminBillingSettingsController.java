package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.BillingSettingsDTO;
import com.retailsystem.dto.BillingSettingsRequest;
import com.retailsystem.service.BillingSettingsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Super Admin -> Billing Settings: the tax rate and the loyalty points
 * earn rate used by every checkout. Locked to ROLE_SUPER_ADMIN by the
 * existing /api/admin/** rule.
 */
@RestController
@RequestMapping("/api/admin/billing-settings")
public class AdminBillingSettingsController {

    @Autowired
    private BillingSettingsService billingSettingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<BillingSettingsDTO>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(billingSettingsService.getSettings()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<BillingSettingsDTO>> updateSettings(@Valid @RequestBody BillingSettingsRequest request) {
        BillingSettingsDTO settings = billingSettingsService.updateSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Billing settings updated", settings));
    }
}
