package com.retailsystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * HR/payroll record for a staff member — distinct from {@link User}, which
 * is the login/authentication account. Not every Employee necessarily has
 * a login (e.g. inventory staff might not need system access), so the two
 * are linked loosely via an optional {@code linkedUser} reference rather
 * than merged into one entity.
 *
 * Uses single-table inheritance so Manager/Cashier/Inventory-Staff share
 * one "employees" table with a discriminator column, per the spec's
 * request to use an abstract base class with role subclasses. The
 * abstract {@link #calculateMonthlyBonus()} is where the subclasses
 * actually differ in behavior (not just in name) — each role earns a
 * different monthly bonus on top of salary + allowances.
 */
@Entity
@Table(name = "employees")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "employee_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Employee extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String employeeCode;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    private LocalDate joiningDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basicSalary;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    /** Optional link to the login account for this staff member, if they have one. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_user_id")
    private User linkedUser;

    protected Employee() {
    }

    protected Employee(String employeeCode, String fullName, String phone, String email, Branch branch,
                        LocalDate joiningDate, BigDecimal basicSalary, BigDecimal allowances, BigDecimal deductions) {
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.branch = branch;
        this.joiningDate = joiningDate;
        this.basicSalary = basicSalary;
        this.allowances = allowances == null ? BigDecimal.ZERO : allowances;
        this.deductions = deductions == null ? BigDecimal.ZERO : deductions;
        this.active = true;
    }

    /** Role-specific monthly bonus — this is what actually differs between subclasses. */
    public abstract BigDecimal calculateMonthlyBonus();

    /** A short label for the role, used by the frontend without needing to know the discriminator value. */
    public abstract String getEmployeeTypeLabel();

    /** basicSalary + allowances + role bonus − deductions. */
    public BigDecimal calculateNetPay() {
        return basicSalary
                .add(allowances)
                .add(calculateMonthlyBonus())
                .subtract(deductions);
    }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getAllowances() { return allowances; }
    public void setAllowances(BigDecimal allowances) { this.allowances = allowances; }
    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public User getLinkedUser() { return linkedUser; }
    public void setLinkedUser(User linkedUser) { this.linkedUser = linkedUser; }
}
