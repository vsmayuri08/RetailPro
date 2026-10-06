package com.retailsystem.repository;

import com.retailsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByPhone(String phone);
    Optional<Customer> findByPhone(String phone);
    List<Customer> findByFullNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);
}
