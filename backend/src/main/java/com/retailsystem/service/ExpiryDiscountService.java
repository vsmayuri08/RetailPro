package com.retailsystem.service;

import com.retailsystem.dto.ExpiryDiscountRuleDTO;
import com.retailsystem.dto.ExpiryDiscountRuleRequest;
import com.retailsystem.entity.ExpiryDiscountRule;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.ResourceNotFoundException;
import com.retailsystem.repository.ExpiryDiscountRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Module 10 -> Super Admin manages the discount ladder (add/edit/
 * activate/deactivate/delete rows); {@link #computeDiscountPercent} is the
 * single place that ladder is actually read, and it's used by both
 * ProductService (to show "current expiry-based discount" in the catalog)
 * and SaleService (to actually apply it at checkout) — so there is exactly
 * one implementation of the discount logic, not two copies that could
 * drift apart.
 */
@Service
public class ExpiryDiscountService {

    @Autowired
    private ExpiryDiscountRuleRepository ruleRepository;

    @Transactional
    public ExpiryDiscountRuleDTO createRule(ExpiryDiscountRuleRequest request) {
        validateRange(request);
        ExpiryDiscountRule rule = new ExpiryDiscountRule(
                request.getLabel(), request.getMinDaysBeforeExpiry(), request.getMaxDaysBeforeExpiry(), request.getDiscountPercent());
        rule = ruleRepository.save(rule);
        return ExpiryDiscountRuleDTO.fromEntity(rule);
    }

    @Transactional
    public ExpiryDiscountRuleDTO updateRule(Long id, ExpiryDiscountRuleRequest request) {
        validateRange(request);
        ExpiryDiscountRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expiry discount rule", "id", id));
        rule.setLabel(request.getLabel());
        rule.setMinDaysBeforeExpiry(request.getMinDaysBeforeExpiry());
        rule.setMaxDaysBeforeExpiry(request.getMaxDaysBeforeExpiry());
        rule.setDiscountPercent(request.getDiscountPercent());
        rule = ruleRepository.save(rule);
        return ExpiryDiscountRuleDTO.fromEntity(rule);
    }

    @Transactional
    public ExpiryDiscountRuleDTO setActive(Long id, boolean active) {
        ExpiryDiscountRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expiry discount rule", "id", id));
        rule.setActive(active);
        rule = ruleRepository.save(rule);
        return ExpiryDiscountRuleDTO.fromEntity(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expiry discount rule", "id", id);
        }
        ruleRepository.deleteById(id);
    }

    public List<ExpiryDiscountRuleDTO> getAllRules() {
        return ruleRepository.findAllByOrderByMinDaysBeforeExpiryAsc().stream()
                .map(ExpiryDiscountRuleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * The discount percent that currently applies to a product with the given expiry date —
     * 0 if it has no expiry date, is more than every rule's range from expiring, or is already
     * expired (expired products aren't "discounted", they're blocked from sale entirely — see
     * SaleService). When multiple active rules could match the same day count (admin
     * misconfiguration), the narrowest/most urgent range wins.
     */
    public BigDecimal computeDiscountPercent(LocalDate expiryDate) {
        if (expiryDate == null) {
            return BigDecimal.ZERO;
        }
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        if (daysUntilExpiry < 0) {
            return BigDecimal.ZERO; // expired — SaleService blocks the sale outright, not a discount case
        }
        return ruleRepository.findByActiveTrueOrderByMinDaysBeforeExpiryAsc().stream()
                .filter(rule -> daysUntilExpiry >= rule.getMinDaysBeforeExpiry() && daysUntilExpiry <= rule.getMaxDaysBeforeExpiry())
                .min((a, b) -> Integer.compare(
                        a.getMaxDaysBeforeExpiry() - a.getMinDaysBeforeExpiry(),
                        b.getMaxDaysBeforeExpiry() - b.getMinDaysBeforeExpiry()))
                .map(ExpiryDiscountRule::getDiscountPercent)
                .orElse(BigDecimal.ZERO);
    }

    private void validateRange(ExpiryDiscountRuleRequest request) {
        if (request.getMinDaysBeforeExpiry() > request.getMaxDaysBeforeExpiry()) {
            throw new BadRequestException("Minimum days cannot be greater than maximum days");
        }
    }
}
