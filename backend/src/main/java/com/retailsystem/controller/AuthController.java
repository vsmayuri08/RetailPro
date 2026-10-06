package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.LoginRequest;
import com.retailsystem.dto.LoginResponse;
import com.retailsystem.dto.UserDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.AuthService;
import com.retailsystem.service.UserService;
import com.retailsystem.service.ActivityLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityLogService activityLogService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        activityLogService.record(response.getId(), response.getFullName(), response.getRole(),
                response.getBranchId(), "LOGIN", "User", String.valueOf(response.getId()),
                "Signed in");
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserDTO userDTO = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }
}
