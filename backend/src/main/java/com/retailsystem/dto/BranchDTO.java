package com.retailsystem.dto;

import com.retailsystem.entity.Branch;

import java.time.LocalDateTime;

public class BranchDTO {

    private Long id;
    private String branchName;
    private String branchCode;
    private String address;
    private String city;
    private String phone;
    private String status;
    private int managerCount;
    private int cashierCount;
    private int employeeCount;
    private LocalDateTime createdAt;

    public static BranchDTO fromEntity(Branch branch) {
        BranchDTO dto = new BranchDTO();
        dto.id = branch.getId();
        dto.branchName = branch.getBranchName();
        dto.branchCode = branch.getBranchCode();
        dto.address = branch.getAddress();
        dto.city = branch.getCity();
        dto.phone = branch.getPhone();
        dto.status = branch.getStatus() != null ? branch.getStatus().name() : null;
        dto.createdAt = branch.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getManagerCount() { return managerCount; }
    public void setManagerCount(int managerCount) { this.managerCount = managerCount; }
    public int getCashierCount() { return cashierCount; }
    public void setCashierCount(int cashierCount) { this.cashierCount = cashierCount; }
    public int getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
