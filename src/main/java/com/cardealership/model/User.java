package com.cardealership.model;

public class User {

    private int id;           // user_id
    private String username;
    private String password;  // SHA-256 hex
    private String role;      // ADMIN | EMPLOYEE | CUSTOMER
    private int empId;        // nullable FK to Employees (0 = not set)
    private int customerId;   // nullable FK to Customers (0 = not set)
    private String createdAt;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getEmpId() { return empId; }
    public void setEmpId(int empId) { this.empId = empId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
