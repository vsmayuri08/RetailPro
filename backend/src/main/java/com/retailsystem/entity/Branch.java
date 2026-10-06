package com.retailsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.retailsystem.enums.BranchStatus;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "branches")
public class Branch extends BaseEntity {

    @Column(nullable = false)
    private String branchName;

    @Column(nullable = false, unique = true)
    private String branchCode;

    private String address;
    private String city;
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BranchStatus status = BranchStatus.ACTIVE;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    public Branch() {
    }

    public Branch(String branchName, String branchCode, String address, String city, String phone, BranchStatus status) {
        this.branchName = branchName;
        this.branchCode = branchCode;
        this.address = address;
        this.city = city;
        this.phone = phone;
        this.status = status;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BranchStatus getStatus() {
        return status;
    }

    public void setStatus(BranchStatus status) {
        this.status = status;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
