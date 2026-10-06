package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.SupplierDTO;
import com.retailsystem.dto.SupplierRequest;
import com.retailsystem.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin endpoints for supplier management.
 * Locked to ROLE_SUPER_ADMIN by the /api/admin/** rule in SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierDTO>> createSupplier(@Valid @RequestBody SupplierRequest request) {
        SupplierDTO supplier = supplierService.createSupplier(request);
        return ResponseEntity.ok(ApiResponse.success("Supplier created successfully", supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDTO>> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        SupplierDTO supplier = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", supplier));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<SupplierDTO>> activateSupplier(@PathVariable Long id) {
        SupplierDTO supplier = supplierService.setSupplierActive(id, true);
        return ResponseEntity.ok(ApiResponse.success("Supplier activated", supplier));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<SupplierDTO>> deactivateSupplier(@PathVariable Long id) {
        SupplierDTO supplier = supplierService.setSupplierActive(id, false);
        return ResponseEntity.ok(ApiResponse.success("Supplier deactivated", supplier));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDTO>> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getSupplier(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> getAllSuppliers(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getAllSuppliers(query)));
    }
}
