package com.cardealership.model;

/**
 * Represents a row in the Employees table.
 *
 * The schema stores emp_id, firstName, lastName, email.
 * For authentication we additionally need username and password_hash,
 * which live in the same table after the refactor adds those columns.
 * Until then, username defaults to the email local-part and password
 * is stored as a SHA-256 hex string in an additional column.
 */
public class Employee {

    private int empId;
    private String firstName;
    private String lastName;
    private String email;

    // Auth fields — stored in Employees table as username / password_hash
    private String username;
    private String passwordHash;
    private String role; // e.g. "Admin", "Salesperson", "Employee" — read from Roles via join

    public Employee() {}

    public Employee(int empId, String firstName, String lastName, String email) {
        this.empId = empId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public int getEmpId()                          { return empId; }
    public void setEmpId(int empId)                { this.empId = empId; }

    public String getFirstName()                   { return firstName; }
    public void setFirstName(String firstName)     { this.firstName = firstName; }

    public String getLastName()                    { return lastName; }
    public void setLastName(String lastName)       { this.lastName = lastName; }

    public String getEmail()                       { return email; }
    public void setEmail(String email)             { this.email = email; }

    public String getUsername()                    { return username; }
    public void setUsername(String username)       { this.username = username; }

    public String getPasswordHash()                { return passwordHash; }
    public void setPasswordHash(String hash)       { this.passwordHash = hash; }

    public String getRole()                        { return role; }
    public void setRole(String role)               { this.role = role; }

    /** Convenience: full display name */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
