package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CustomerDTO;
import com.retailsystem.dto.CustomerRequest;
import com.retailsystem.dto.LoyaltyAdjustmentRequest;
import com.retailsystem.dto.SaleDTO;
import com.retailsystem.service.CustomerService;
import com.retailsystem.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer registration and search. Lives under /api/manager/customers so
 * it reuses the existing hasAnyRole(SUPER_ADMIN, BRANCH_MANAGER) rule in
 * SecurityConfig. Cashiers register/search customers through the separate
 * CashierCustomerController (/api/cashier/customers), which calls the same
 * CustomerService.
 */
@RestController
@RequestMapping("/api/manager/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SaleService saleService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> registerCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerDTO customer = customerService.registerCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("Customer registered successfully", customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        CustomerDTO customer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", customer));
    }

    @PatchMapping("/{id}/loyalty-points")
    public ResponseEntity<ApiResponse<CustomerDTO>> adjustLoyaltyPoints(@PathVariable Long id,
                                                                         @Valid @RequestBody LoyaltyAdjustmentRequest request) {
        CustomerDTO customer = customerService.adjustLoyaltyPoints(id, request);
        return ResponseEntity.ok(ApiResponse.success("Loyalty points updated", customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomer(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> searchCustomers(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.success(customerService.searchCustomers(query)));
    }

    /** Module 7 -> "Show customer purchase history". Now backed by real Sale data. */
    @GetMapping("/{id}/purchases")
    public ResponseEntity<ApiResponse<List<SaleDTO>>> getPurchaseHistory(@PathVariable Long id) {
        customerService.getCustomer(id); // 404s cleanly if the customer doesn't exist
        return ResponseEntity.ok(ApiResponse.success(saleService.getSalesForCustomer(id)));
    }
}
