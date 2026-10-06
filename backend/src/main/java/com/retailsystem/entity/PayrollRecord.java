package com.retailsystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * A generated payslip for one employee for one calendar month. Snapshots
 * the salary figures at generation time (rather than recomputing live from
 * the current Employee record), since an employee's salary can change
 * later and past payslips shouldn't retroactively change with it.
 * Generation is idempotent per (employee, month, year) — see
 * PayrollService#generatePayroll.
 */
@Entity
@Table(name = "payroll_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payroll_employee_period", columnNames = {"employee_id", "period_month", "period_year"})
})
public class PayrollRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "period_month", nullable = false)
    private int periodMonth;

    @Column(name = "period_year", nullable = false)
    private int periodYear;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal allowances;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal bonus;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deductions;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netPay;

    public PayrollRecord() {
    }

    public PayrollRecord(Employee employee, int periodMonth, int periodYear, BigDecimal basicSalary,
                          BigDecimal allowances, BigDecimal bonus, BigDecimal deductions, BigDecimal netPay) {
        this.employee = employee;
        this.periodMonth = periodMonth;
        this.periodYear = periodYear;
        this.basicSalary = basicSalary;
        this.allowances = allowances;
        this.bonus = bonus;
        this.deductions = deductions;
        this.netPay = netPay;
    }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
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
}
