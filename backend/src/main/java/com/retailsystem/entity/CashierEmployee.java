package com.retailsystem.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("CASHIER")
public class CashierEmployee extends Employee {

    private static final BigDecimal SHIFT_ALLOWANCE = new BigDecimal("1500.00");

    public CashierEmployee() {
        super();
    }

    public CashierEmployee(String employeeCode, String fullName, String phone, String email, Branch branch,
                            LocalDate joiningDate, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions) {
        super(employeeCode, fullName, phone, email, branch, joiningDate, basicSalary, allowances, deductions);
    }

    /** Cashiers earn a fixed monthly shift allowance, regardless of salary band. */
    @Override
    public BigDecimal calculateMonthlyBonus() {
        return SHIFT_ALLOWANCE;
    }

    @Override
    public String getEmployeeTypeLabel() {
        return "Cashier";
    }
}
