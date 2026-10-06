package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.SupplierDTO;
import com.retailsystem.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only supplier list for Branch Managers, so the "Receive Stock" form
 * can offer a supplier picker — full supplier CRUD stays Super-Admin-only
 * via SupplierController (/api/admin/suppliers). Reuses the existing
 * hasAnyRole(SUPER_ADMIN, BRANCH_MANAGER) rule for /api/manager/**.
 */
@RestController
@RequestMapping("/api/manager/suppliers")
public class ManagerSupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> getAllSuppliers() {
        return ResponseEntity.ok(ApiResponse.success(supplierService.getAllSuppliers(null)));
    }
}
