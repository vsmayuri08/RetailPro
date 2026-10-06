package com.retailsystem.dto;

import com.retailsystem.entity.Supplier;

import java.time.LocalDateTime;

public class SupplierDTO {

    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String gstNumber;
    private boolean active;
    private LocalDateTime createdAt;

    public static SupplierDTO fromEntity(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.id = supplier.getId();
        dto.name = supplier.getName();
        dto.contactPerson = supplier.getContactPerson();
        dto.phone = supplier.getPhone();
        dto.email = supplier.getEmail();
        dto.address = supplier.getAddress();
        dto.gstNumber = supplier.getGstNumber();
        dto.active = supplier.isActive();
        dto.createdAt = supplier.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
