package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.CreateManagerRequest;
import com.retailsystem.dto.UserDTO;
import com.retailsystem.enums.Role;
import com.retailsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin endpoints for managing users: creating branch managers and
 * viewing/activating/deactivating any account in the organization.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @PostMapping("/managers")
    public ResponseEntity<ApiResponse<UserDTO>> createBranchManager(@Valid @RequestBody CreateManagerRequest request) {
        UserDTO manager = userService.createBranchManager(request);
        return ResponseEntity.ok(ApiResponse.success("Branch manager created successfully", manager));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Long branchId) {
        List<UserDTO> users;
        if (role != null) {
            users = userService.getUsersByRole(role);
        } else if (branchId != null) {
            users = userService.getUsersByBranch(branchId);
        } else {
            users = userService.getAllUsers();
        }
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<UserDTO>> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User activated", userService.setUserActive(id, true)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserDTO>> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User deactivated", userService.setUserActive(id, false)));
    }
}
