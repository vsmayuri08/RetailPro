package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.BillingSettingsDTO;
import com.retailsystem.service.BillingSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only view of billing settings for the checkout screen, so the
 * cashier can show a live tax/total preview before submitting the sale
 * (the real, authoritative figures are still computed server-side in
 * SaleService#checkout — this is only a preview). Editing stays
 * Super-Admin-only via AdminBillingSettingsController.
 */
@RestController
@RequestMapping("/api/cashier/billing-settings")
public class CashierBillingSettingsController {

    @Autowired
    private BillingSettingsService billingSettingsService;

    @GetMapping
    public ResponseEntity<ApiResponse<BillingSettingsDTO>> getSettings() {
        return ResponseEntity.ok(ApiResponse.success(billingSettingsService.getSettings()));
    }
}
