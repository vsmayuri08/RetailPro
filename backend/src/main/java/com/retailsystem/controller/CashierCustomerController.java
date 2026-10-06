package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CustomerDTO;
import com.retailsystem.dto.CustomerRequest;
import com.retailsystem.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cashier -> "Select or register customers". Same CustomerService the
 * Branch Manager screen uses (customers are org-wide, not branch-scoped),
 * just exposed under /api/cashier/** so the existing security rule for
 * that role applies without touching SecurityConfig.
 */
@RestController
@RequestMapping("/api/cashier/customers")
public class CashierCustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> registerCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerDTO customer = customerService.registerCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Customer registered successfully", customer));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> searchCustomers(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.success(customerService.searchCustomers(query)));
    }
}
