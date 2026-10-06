package com.retailsystem.dto;

public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private Long branchId;
    private String branchName;

    public LoginResponse() {
    }

    public LoginResponse(String token, String type, Long id, String fullName, String email, String role, Long branchId, String branchName) {
        this.token = token;
        this.type = type;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.branchId = branchId;
        this.branchName = branchName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String fullName;
        private String email;
        private String role;
        private Long branchId;
        private String branchName;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder branchId(Long branchId) {
            this.branchId = branchId;
            return this;
        }

        public Builder branchName(String branchName) {
            this.branchName = branchName;
            return this;
        }

        public LoginResponse build() {
            return new LoginResponse(token, type, id, fullName, email, role, branchId, branchName);
        }
    }
}
