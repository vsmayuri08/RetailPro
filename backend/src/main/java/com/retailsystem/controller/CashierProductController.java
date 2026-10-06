package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.ProductDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cashier -> "Search products" / "Barcode or SKU search" / "Available stock
 * display". Read-only, scoped to the cashier's own branch, reusing
 * ProductService's existing branch-scoped search (name/SKU both match
 * since a SKU search is just a more specific name-shaped query here).
 */
@RestController
@RequestMapping("/api/cashier/products")
public class CashierProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(@AuthenticationPrincipal CustomUserDetails principal,
                                                                          @RequestParam(required = false) String query) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        List<ProductDTO> products = productService.searchBranchCatalog(branchId, query);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<ProductDTO>> lookupByScanCode(@AuthenticationPrincipal CustomUserDetails principal,
                                                                      @RequestParam String code) {
        Long branchId = ManagerProductController.requireBranchId(principal);
        ProductDTO product = productService.lookupByScanCode(branchId, code);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
}
