package com.retailsystem.dto;

import jakarta.validation.constraints.NotBlank;

public class BranchRequest {

    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Branch code is required")
    private String branchCode;

    private String address;
    private String city;
    private String phone;

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
}
