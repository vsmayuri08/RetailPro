package com.retailsystem.entity;

import com.retailsystem.enums.LoyaltyTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * A customer of the retail chain. Org-wide, not branch-scoped — the same
 * customer can shop at any branch, so their record (and loyalty balance)
 * needs to be visible chain-wide rather than tied to one branch.
 *
 * Registration date is inherited from BaseEntity#getCreatedAt(). Purchase
 * history isn't tracked yet — that needs the Sale entity from the Billing
 * module, which doesn't exist in this codebase yet.
 */
@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_phone", columnNames = "phone")
})
public class Customer extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phone;

    private String email;

    private String address;

    @Column(nullable = false)
    private int loyaltyPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoyaltyTier loyaltyTier = LoyaltyTier.BRONZE;

    public Customer() {
    }

    public Customer(String fullName, String phone, String email, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.loyaltyPoints = 0;
        this.loyaltyTier = LoyaltyTier.BRONZE;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public LoyaltyTier getLoyaltyTier() { return loyaltyTier; }
    public void setLoyaltyTier(LoyaltyTier loyaltyTier) { this.loyaltyTier = loyaltyTier; }
}
