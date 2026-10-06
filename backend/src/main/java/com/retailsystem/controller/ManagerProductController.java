package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.InventoryBatchDTO;
import com.retailsystem.dto.ProductDTO;
import com.retailsystem.dto.ProductRequest;
import com.retailsystem.dto.ReceiveStockRequest;
import com.retailsystem.dto.StockAdjustmentRequest;
import com.retailsystem.exception.BadRequestException;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.InventoryBatchService;
import com.retailsystem.service.ProductService;
import com.retailsystem.service.ActivityLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Branch Manager endpoints for their own branch's product catalog and
 * inventory. Locked to SUPER_ADMIN/BRANCH_MANAGER by /api/manager/** in
 * SecurityConfig; the branch itself always comes from the caller's JWT,
 * never from the request body or path, so a manager can only ever act on
 * "their assigned branch".
 */
@RestController
@RequestMapping("/api/manager/products")
public class ManagerProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryBatchService inventoryBatchService;

    @Autowired
    private ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@AuthenticationPrincipal CustomUserDetails principal,
                                                                   @Valid @RequestBody ProductRequest request) {
        ProductDTO product = productService.createProduct(requireBranchId(principal), request);
        activityLogService.record(principal, "PRODUCT_CREATED", "Product", String.valueOf(product.getId()),
                product.getName() + " (" + product.getSku() + ")");
        return ResponseEntity.ok(ApiResponse.success("Product created successfully", product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@AuthenticationPrincipal CustomUserDetails principal,
                                                                   @PathVariable Long id,
                                                                   @Valid @RequestBody ProductRequest request) {
        ProductDTO product = productService.updateProduct(requireBranchId(principal), id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", product));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<ProductDTO>> activateProduct(@AuthenticationPrincipal CustomUserDetails principal,
                                                                     @PathVariable Long id) {
        ProductDTO product = productService.setProductActive(requireBranchId(principal), id, true);
        return ResponseEntity.ok(ApiResponse.success("Product activated", product));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<ProductDTO>> deactivateProduct(@AuthenticationPrincipal CustomUserDetails principal,
                                                                       @PathVariable Long id) {
        ProductDTO product = productService.setProductActive(requireBranchId(principal), id, false);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated", product));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<ProductDTO>> adjustStock(@AuthenticationPrincipal CustomUserDetails principal,
                                                                 @PathVariable Long id,
                                                                 @Valid @RequestBody StockAdjustmentRequest request) {
        ProductDTO product = productService.adjustStock(requireBranchId(principal), id, request);
        return ResponseEntity.ok(ApiResponse.success("Inventory updated", product));
    }

    /** Module 5 -> "Add stock" / "Record stock received": opens a new InventoryBatch. */
    @PostMapping("/{id}/receive-stock")
    public ResponseEntity<ApiResponse<InventoryBatchDTO>> receiveStock(@AuthenticationPrincipal CustomUserDetails principal,
                                                                        @PathVariable Long id,
                                                                        @Valid @RequestBody ReceiveStockRequest request) {
        InventoryBatchDTO batch = inventoryBatchService.receiveStock(requireBranchId(principal), id, request);
        activityLogService.record(principal, "STOCK_RECEIVED", "InventoryBatch", String.valueOf(batch.getId()),
                batch.getBatchNumber() + " · qty " + batch.getQuantityReceived());
        return ResponseEntity.ok(ApiResponse.success("Stock received", batch));
    }

    /** Module 5 -> "View stock history": every batch ever received for this product. */
    @GetMapping("/{id}/stock-history")
    public ResponseEntity<ApiResponse<List<InventoryBatchDTO>>> getStockHistory(@AuthenticationPrincipal CustomUserDetails principal,
                                                                                 @PathVariable Long id) {
        List<InventoryBatchDTO> history = inventoryBatchService.getStockHistory(requireBranchId(principal), id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@AuthenticationPrincipal CustomUserDetails principal,
                                                                @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProduct(requireBranchId(principal), id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts(requireBranchId(principal))));
    }

    /** Low-stock / near-expiry / expired items for this branch. */
    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAlerts(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAlerts(requireBranchId(principal))));
    }

    /** Browse another branch's active catalog when building a stock transfer request. */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> browseBranchCatalog(@PathVariable Long branchId,
                                                                               @RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchBranchCatalog(branchId, query)));
    }

    static Long requireBranchId(CustomUserDetails principal) {
        Long branchId = principal.getBranchId();
        if (branchId == null) {
            throw new BadRequestException("This account is not assigned to a branch");
        }
        return branchId;
    }
}
