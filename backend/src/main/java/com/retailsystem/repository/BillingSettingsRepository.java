package com.retailsystem.repository;

import com.retailsystem.entity.BillingSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingSettingsRepository extends JpaRepository<BillingSettings, Long> {
}
