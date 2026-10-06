package com.retailsystem.service;

import com.retailsystem.dto.BillingSettingsDTO;
import com.retailsystem.dto.BillingSettingsRequest;
import com.retailsystem.entity.BillingSettings;
import com.retailsystem.repository.BillingSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Super Admin -> Billing Settings: tax rate and the loyalty points earn
 * rate, both editable rather than hard-coded (see BillingSettings' Javadoc).
 * Single row, created with defaults on first access if it doesn't exist yet.
 */
@Service
public class BillingSettingsService {

    @Autowired
    private BillingSettingsRepository billingSettingsRepository;

    @Transactional
    public BillingSettings getOrCreateSettings() {
        return billingSettingsRepository.findAll().stream().findFirst()
                .orElseGet(() -> billingSettingsRepository.save(new BillingSettings()));
    }

    public BillingSettingsDTO getSettings() {
        return BillingSettingsDTO.fromEntity(getOrCreateSettings());
    }

    @Transactional
    public BillingSettingsDTO updateSettings(BillingSettingsRequest request) {
        BillingSettings settings = getOrCreateSettings();
        settings.setTaxRatePercent(request.getTaxRatePercent());
        settings.setLoyaltyPointsPerAmount(request.getLoyaltyPointsPerAmount());
        settings.setLoyaltyAmountThreshold(request.getLoyaltyAmountThreshold());
        settings = billingSettingsRepository.save(settings);
        return BillingSettingsDTO.fromEntity(settings);
    }
}
