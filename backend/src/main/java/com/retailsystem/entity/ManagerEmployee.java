package com.retailsystem.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("MANAGER")
public class ManagerEmployee extends Employee {

    private static final BigDecimal MANAGEMENT_BONUS_RATE = new BigDecimal("0.10");

    public ManagerEmployee() {
        super();
    }

    public ManagerEmployee(String employeeCode, String fullName, String phone, String email, Branch branch,
                            LocalDate joiningDate, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions) {
        super(employeeCode, fullName, phone, email, branch, joiningDate, basicSalary, allowances, deductions);
    }

    /** Managers earn a 10% management bonus on top of their basic salary. */
    @Override
    public BigDecimal calculateMonthlyBonus() {
        return getBasicSalary().multiply(MANAGEMENT_BONUS_RATE);
    }

    @Override
    public String getEmployeeTypeLabel() {
        return "Branch Manager";
    }
}
