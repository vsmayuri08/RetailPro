package com.retailsystem.dto;

import com.retailsystem.enums.EmployeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeRequest {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Employee type is required")
    private EmployeeType employeeType;

    private String phone;

    private String email;

    /** Only used by the Super Admin endpoint, which may assign any branch. The Branch Manager
     *  endpoint ignores this and always uses the caller's own branch. */
    private Long branchId;

    private LocalDate joiningDate;

    @NotNull(message = "Basic salary is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Basic salary cannot be negative")
    private BigDecimal basicSalary;

    @DecimalMin(value = "0.0", inclusive = true, message = "Allowances cannot be negative")
    private BigDecimal allowances = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Deductions cannot be negative")
    private BigDecimal deductions = BigDecimal.ZERO;

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public EmployeeType getEmployeeType() { return employeeType; }
    public void setEmployeeType(EmployeeType employeeType) { this.employeeType = employeeType; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getAllowances() { return allowances; }
    public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
}
