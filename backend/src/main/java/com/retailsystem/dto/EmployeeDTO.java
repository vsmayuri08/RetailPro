package com.retailsystem.dto;

import com.retailsystem.entity.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeDTO {

    private Long id;
    private String employeeCode;
    private String fullName;
    private String employeeType;
    private String employeeTypeLabel;
    private String phone;
    private String email;
    private Long branchId;
    private String branchName;
    private LocalDate joiningDate;
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal deductions;
    private BigDecimal monthlyBonus;
    private BigDecimal netPay;
    private boolean active;
    private LocalDateTime createdAt;

    public static EmployeeDTO fromEntity(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.id = employee.getId();
        dto.employeeCode = employee.getEmployeeCode();
        dto.fullName = employee.getFullName();
        dto.employeeType = employee.getClass().getSimpleName();
        dto.employeeTypeLabel = employee.getEmployeeTypeLabel();
        dto.phone = employee.getPhone();
        dto.email = employee.getEmail();
        dto.branchId = employee.getBranch() != null ? employee.getBranch().getId() : null;
        dto.branchName = employee.getBranch() != null ? employee.getBranch().getBranchName() : null;
        dto.joiningDate = employee.getJoiningDate();
        dto.basicSalary = employee.getBasicSalary();
        dto.allowances = employee.getAllowances();
        dto.deductions = employee.getDeductions();
        dto.monthlyBonus = employee.calculateMonthlyBonus();
        dto.netPay = employee.calculateNetPay();
        dto.active = employee.isActive();
        dto.createdAt = employee.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }
    public String getEmployeeTypeLabel() { return employeeTypeLabel; }
    public void setEmployeeTypeLabel(String employeeTypeLabel) { this.employeeTypeLabel = employeeTypeLabel; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getAllowances() { return allowances; }
    public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public BigDecimal getMonthlyBonus() { return monthlyBonus; }
    public void setMonthlyBonus(BigDecimal monthlyBonus) { this.monthlyBonus = monthlyBonus; }
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
