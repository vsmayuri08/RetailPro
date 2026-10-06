package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.EmployeeDTO;
import com.retailsystem.dto.EmployeeRequest;
import com.retailsystem.dto.GeneratePayrollRequest;
import com.retailsystem.dto.PayrollRecordDTO;
import com.retailsystem.security.CustomUserDetails;
import com.retailsystem.service.EmployeeService;
import com.retailsystem.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Branch Manager -> Employee HR records + payroll, scoped to their own
 * branch only. Reuses the existing hasAnyRole(SUPER_ADMIN, BRANCH_MANAGER)
 * rule for /api/manager/** — no SecurityConfig changes needed.
 *
 * Distinct from ManagerEmployeeController (/api/manager/employees), which
 * creates Cashier login accounts — this manages HR/payroll records
 * (Employee), not authentication accounts (User). See the README.
 */
@RestController
@RequestMapping("/api/manager/staff")
public class ManagerStaffController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayrollService payrollService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(@AuthenticationPrincipal CustomUserDetails principal,
                                                                     @Valid @RequestBody EmployeeRequest request) {
        EmployeeDTO employee = employeeService.createEmployee(request, ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success("Employee created successfully", employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(@AuthenticationPrincipal CustomUserDetails principal,
                                                                     @PathVariable Long id,
                                                                     @Valid @RequestBody EmployeeRequest request) {
        EmployeeDTO employee = employeeService.updateEmployee(id, request, ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", employee));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<EmployeeDTO>> activateEmployee(@AuthenticationPrincipal CustomUserDetails principal,
                                                                       @PathVariable Long id) {
        EmployeeDTO employee = employeeService.setActive(id, true, ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success("Employee activated", employee));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<EmployeeDTO>> deactivateEmployee(@AuthenticationPrincipal CustomUserDetails principal,
                                                                         @PathVariable Long id) {
        EmployeeDTO employee = employeeService.setActive(id, false, ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated", employee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployee(@AuthenticationPrincipal CustomUserDetails principal,
                                                                  @PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployee(id, ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployees(@AuthenticationPrincipal CustomUserDetails principal) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByBranch(ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    @PostMapping("/{id}/payroll")
    public ResponseEntity<ApiResponse<PayrollRecordDTO>> generatePayroll(@AuthenticationPrincipal CustomUserDetails principal,
                                                                          @PathVariable Long id,
                                                                          @Valid @RequestBody GeneratePayrollRequest request) {
        PayrollRecordDTO record = payrollService.generatePayroll(id, request, ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success("Payroll generated", record));
    }

    @GetMapping("/{id}/payroll")
    public ResponseEntity<ApiResponse<List<PayrollRecordDTO>>> getPayrollHistory(@AuthenticationPrincipal CustomUserDetails principal,
                                                                                  @PathVariable Long id) {
        List<PayrollRecordDTO> history = payrollService.getPayrollHistory(id, ManagerProductController.requireBranchId(principal));
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
