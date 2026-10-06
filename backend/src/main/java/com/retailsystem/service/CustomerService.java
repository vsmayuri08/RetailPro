package com.retailsystem.service;

import com.retailsystem.dto.CustomerDTO;
import com.retailsystem.dto.CustomerRequest;
import com.retailsystem.dto.LoyaltyAdjustmentRequest;
import com.retailsystem.entity.Customer;
import com.retailsystem.enums.LoyaltyTier;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.exception.CustomerNotFoundException;
import com.retailsystem.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Customer registration, search, and loyalty tier management. Customers
 * are org-wide (not branch-scoped) since the same person can shop at any
 * branch — accessible to Branch Managers and Super Admin today; will also
 * open up to Cashiers once that role's screens are built.
 *
 * Loyalty points aren't wired to real purchases yet since the Billing
 * module (Sale/SaleItem entities) doesn't exist in this codebase. The
 * tier-recalculation logic lives here in one place so a future
 * "sale completed" hook only needs to call {@link #adjustLoyaltyPoints}
 * rather than re-implement tier thresholds elsewhere.
 */
@Service
public class CustomerService {

    /** Loyalty tier thresholds, in points. Adjust here if the business rules change. */
    private static final int SILVER_THRESHOLD = 500;
    private static final int GOLD_THRESHOLD = 2000;
    private static final int PLATINUM_THRESHOLD = 5000;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public CustomerDTO registerCustomer(CustomerRequest request) {
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("A customer with phone number '" + request.getPhone() + "' already exists");
        }
        Customer customer = new Customer(request.getFullName(), request.getPhone(), request.getEmail(), request.getAddress());
        customer = customerRepository.save(customer);
        return CustomerDTO.fromEntity(customer);
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        if (!customer.getPhone().equals(request.getPhone()) && customerRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("A customer with phone number '" + request.getPhone() + "' already exists");
        }

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer = customerRepository.save(customer);
        return CustomerDTO.fromEntity(customer);
    }

    /** Adds (or deducts, with a negative delta) loyalty points and recalculates the tier. */
    @Transactional
    public CustomerDTO adjustLoyaltyPoints(Long id, LoyaltyAdjustmentRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        int newPoints = customer.getLoyaltyPoints() + request.getDelta();
        if (newPoints < 0) {
            throw new BadRequestException("Adjustment would leave loyalty points below zero (current: " + customer.getLoyaltyPoints() + ")");
        }
        customer.setLoyaltyPoints(newPoints);
        customer.setLoyaltyTier(tierForPoints(newPoints));
        customer = customerRepository.save(customer);
        return CustomerDTO.fromEntity(customer);
    }

    public CustomerDTO getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return CustomerDTO.fromEntity(customer);
    }

    /** Search by name or phone (spec: "search customers using phone number or name"). */
    public List<CustomerDTO> searchCustomers(String query) {
        List<Customer> customers = (query == null || query.isBlank())
                ? customerRepository.findAll()
                : customerRepository.findByFullNameContainingIgnoreCaseOrPhoneContaining(query, query);
        return customers.stream().map(CustomerDTO::fromEntity).collect(Collectors.toList());
    }

    private LoyaltyTier tierForPoints(int points) {
        if (points >= PLATINUM_THRESHOLD) return LoyaltyTier.PLATINUM;
        if (points >= GOLD_THRESHOLD) return LoyaltyTier.GOLD;
        if (points >= SILVER_THRESHOLD) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }
}
