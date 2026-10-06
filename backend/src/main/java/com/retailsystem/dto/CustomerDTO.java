package com.retailsystem.dto;

import com.retailsystem.entity.Customer;

import java.time.LocalDateTime;

public class CustomerDTO {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private int loyaltyPoints;
    private String loyaltyTier;
    private LocalDateTime registeredAt;

    public static CustomerDTO fromEntity(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.id = customer.getId();
        dto.fullName = customer.getFullName();
        dto.phone = customer.getPhone();
        dto.email = customer.getEmail();
        dto.address = customer.getAddress();
        dto.loyaltyPoints = customer.getLoyaltyPoints();
        dto.loyaltyTier = customer.getLoyaltyTier().name();
        dto.registeredAt = customer.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getLoyaltyTier() { return loyaltyTier; }
    public void setLoyaltyTier(String loyaltyTier) { this.loyaltyTier = loyaltyTier; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
