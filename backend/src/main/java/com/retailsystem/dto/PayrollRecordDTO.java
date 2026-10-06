package com.retailsystem.dto;

import com.retailsystem.entity.PayrollRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayrollRecordDTO {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private int periodMonth;
    private int periodYear;
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal netPay;
    private LocalDateTime generatedAt;

    public static PayrollRecordDTO fromEntity(PayrollRecord record) {
        PayrollRecordDTO dto = new PayrollRecordDTO();
        dto.id = record.getId();
        dto.employeeId = record.getEmployee().getId();
        dto.employeeName = record.getEmployee().getFullName();
        dto.employeeCode = record.getEmployee().getEmployeeCode();
        dto.periodMonth = record.getPeriodMonth();
        dto.periodYear = record.getPeriodYear();
        dto.basicSalary = record.getBasicSalary();
        dto.allowances = record.getAllowances();
        dto.bonus = record.getBonus();
        dto.deductions = record.getDeductions();
        dto.netPay = record.getNetPay();
        dto.generatedAt = record.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public int getPeriodMonth() { return periodMonth; }
    public void setPeriodMonth(int periodMonth) { this.periodMonth = periodMonth; }
    public int getPeriodYear() { return periodYear; }
    public void setPeriodYear(int periodYear) { this.periodYear = periodYear; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getAllowances() { return allowances; }
    public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
    public BigDecimal getBonus() { return bonus; }
    public void setBonus(BigDecimal bonus) { this.bonus = bonus; }
    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
