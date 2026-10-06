package com.retailsystem.controller;

import com.retailsystem.dto.ApiResponse;
import com.retailsystem.dto.EmployeeDTO;
import com.retailsystem.dto.EmployeeRequest;
import com.retailsystem.dto.GeneratePayrollRequest;
import com.retailsystem.dto.PayrollRecordDTO;
import com.retailsystem.service.EmployeeService;
import com.retailsystem.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin -> org-wide Employee HR management (any type, any branch) and
 * payroll generation. Locked to ROLE_SUPER_ADMIN by the existing
 * /api/admin/** rule in SecurityConfig.
 *
 * This is a separate concept from AdminUserController — that manages login
 * accounts (User), this manages HR/payroll records (Employee). See the
 * README for why the two aren't merged in this drop.
 */
@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PayrollService payrollService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeDTO employee = employeeService.createEmployee(request, null);
        return ResponseEntity.ok(ApiResponse.success("Employee created successfully", employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        EmployeeDTO employee = employeeService.updateEmployee(id, request, null);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", employee));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<EmployeeDTO>> activateEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Employee activated", employeeService.setActive(id, true, null)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<EmployeeDTO>> deactivateEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated", employeeService.setActive(id, false, null)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployee(id, null)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAllEmployees()));
    }

    @PostMapping("/{id}/payroll")
    public ResponseEntity<ApiResponse<PayrollRecordDTO>> generatePayroll(@PathVariable Long id,
                                                                          @Valid @RequestBody GeneratePayrollRequest request) {
        PayrollRecordDTO record = payrollService.generatePayroll(id, request, null);
        return ResponseEntity.ok(ApiResponse.success("Payroll generated", record));
    }

    @GetMapping("/{id}/payroll")
    public ResponseEntity<ApiResponse<List<PayrollRecordDTO>>> getPayrollHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayrollHistory(id, null)));
    }
}
