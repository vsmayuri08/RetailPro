package com.retailsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A supplier that inventory batches can be sourced from. Org-wide, managed
 * by the Super Admin — not branch-scoped, since the same supplier may
 * deliver stock to multiple branches.
 */
@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;

    /** GST registration number — optional, since not every supplier is GST-registered. */
    private String gstNumber;

    @Column(nullable = false)
    private boolean active = true;

    public Supplier() {
    }

    public Supplier(String name, String contactPerson, String phone, String email, String address, String gstNumber) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.gstNumber = gstNumber;
        this.active = true;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
