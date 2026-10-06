package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CategoryDTO;
import com.retailsystem.dto.CategoryRequest;
import com.retailsystem.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin endpoints for category management.
 * Locked to ROLE_SUPER_ADMIN by the /api/admin/** rule in SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryDTO category = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        CategoryDTO category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CategoryDTO>> activateCategory(@PathVariable Long id) {
        CategoryDTO category = categoryService.setCategoryActive(id, true);
        return ResponseEntity.ok(ApiResponse.success("Category activated", category));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CategoryDTO>> deactivateCategory(@PathVariable Long id) {
        CategoryDTO category = categoryService.setCategoryActive(id, false);
        return ResponseEntity.ok(ApiResponse.success("Category deactivated", category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategory(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }
}
