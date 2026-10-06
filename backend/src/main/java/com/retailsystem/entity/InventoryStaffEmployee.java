package com.retailsystem.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("INVENTORY_STAFF")
public class InventoryStaffEmployee extends Employee {

    private static final BigDecimal HANDLING_ALLOWANCE = new BigDecimal("1000.00");

    public InventoryStaffEmployee() {
        super();
    }

    public InventoryStaffEmployee(String employeeCode, String fullName, String phone, String email, Branch branch,
                                   LocalDate joiningDate, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions) {
        super(employeeCode, fullName, phone, email, branch, joiningDate, basicSalary, allowances, deductions);
    }

    /** Inventory staff earn a fixed monthly handling allowance for stock work. */
    @Override
    public BigDecimal calculateMonthlyBonus() {
        return HANDLING_ALLOWANCE;
    }

    @Override
    public String getEmployeeTypeLabel() {
        return "Inventory Staff";
    }
}
