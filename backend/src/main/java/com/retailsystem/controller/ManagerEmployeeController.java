package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CreateEmployeeRequest;
import com.retailsystem.dto.UserDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Branch Manager -> "Add employees" (cashiers), scoped to the manager's own branch. */
@RestController
@RequestMapping("/api/manager/employees")
public class ManagerEmployeeController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createEmployee(@AuthenticationPrincipal CustomUserDetails principal,
                                                                 @Valid @RequestBody CreateEmployeeRequest request) {
        UserDTO employee = userService.createEmployee(ManagerProductController.requireBranchId(principal), request);
        return ResponseEntity.ok(ApiResponse.success("Employee created successfully", employee));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getEmployees(@AuthenticationPrincipal CustomUserDetails principal) {
        List<UserDTO> employees = userService.getEmployeesByBranch(ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<UserDTO>> activateEmployee(@AuthenticationPrincipal CustomUserDetails principal,
                                                                   @PathVariable Long id) {
        UserDTO employee = userService.setEmployeeActiveInBranch(ManagerProductController.requireBranchId(principal), id, true);
        return ResponseEntity.ok(ApiResponse.success("Employee activated", employee));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserDTO>> deactivateEmployee(@AuthenticationPrincipal CustomUserDetails principal,
                                                                     @PathVariable Long id) {
        UserDTO employee = userService.setEmployeeActiveInBranch(ManagerProductController.requireBranchId(principal), id, false);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated", employee));
    }
}
