package com.retailsystem.repository;

import com.retailsystem.entity.ExpiryDiscountRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpiryDiscountRuleRepository extends JpaRepository<ExpiryDiscountRule, Long> {
    List<ExpiryDiscountRule> findByActiveTrueOrderByMinDaysBeforeExpiryAsc();
    List<ExpiryDiscountRule> findAllByOrderByMinDaysBeforeExpiryAsc();
}
