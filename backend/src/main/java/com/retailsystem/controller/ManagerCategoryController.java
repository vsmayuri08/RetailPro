package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CategoryDTO;
import com.retailsystem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only category list for Branch Managers, so the product create/edit
 * form can offer a category picker — full category CRUD stays
 * Super-Admin-only via CategoryController (/api/admin/categories). Reuses
 * the existing hasAnyRole(SUPER_ADMIN, BRANCH_MANAGER) rule for
 * /api/manager/**.
 */
@RestController
@RequestMapping("/api/manager/categories")
public class ManagerCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }
}
