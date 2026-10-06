package com.retailsystem.dto;

import com.retailsystem.entity.Branch;

/**
 * Minimal branch info exposed to Branch Managers (e.g. for the "request
 * stock from another branch" dropdown) — no staff counts or contact
 * details, unlike the Super Admin's BranchDTO.
 */
public class BranchLiteDTO {

    private Long id;
    private String branchName;
    private String branchCode;
    private String city;

    public static BranchLiteDTO fromEntity(Branch branch) {
        BranchLiteDTO dto = new BranchLiteDTO();
        dto.id = branch.getId();
        dto.branchName = branch.getBranchName();
        dto.branchCode = branch.getBranchCode();
        dto.city = branch.getCity();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
