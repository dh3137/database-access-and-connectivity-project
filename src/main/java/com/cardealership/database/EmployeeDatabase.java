package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.Employee;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the Employees table.
 *
 * Authentication is performed here by looking up username + password_hash
 * columns that are added to the Employees table (see schema note in Employee.java).
 * The first role assigned to the employee is also fetched via a JOIN on EmployeeRoles
 * and Roles so that the session knows whether the employee is an Admin, Salesperson, etc.
 */
public class EmployeeDatabase {

    private final MySQLDatabase database;

    public EmployeeDatabase(MySQLDatabase database) {
        this.database = database;
    }

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Looks up an employee by username + hashed password.
     * Returns the employee (with role populated from EmployeeRoles) or {@code null}.
     *
     * Relies on the Employees table having {@code username} and {@code password_hash}
     * columns (added in schema migration for Issue 1).
     */
    public Employee authenticate(String username, String passwordHash) throws DLException {
        String sql =
            "SELECT e.emp_id, e.firstName, e.lastName, e.email, e.username, e.password_hash, " +
            "       COALESCE(r.role_name, 'Employee') AS role_name " +
            "FROM Employees e " +
            "LEFT JOIN EmployeeRoles er ON e.emp_id = er.emp_id " +
            "LEFT JOIN Roles r ON er.role_id = r.role_id " +
            "WHERE e.username = ? AND e.password_hash = ? " +
            "LIMIT 1";
        ArrayList<String> values = new ArrayList<>();
        values.add(username);
        values.add(passwordHash);
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapEmployee(rows[1]) : null;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<Employee> getAllEmployees() throws DLException {
        String sql =
            "SELECT e.emp_id, e.firstName, e.lastName, e.email, e.username, e.password_hash, " +
            "       COALESCE(r.role_name, 'Employee') AS role_name " +
            "FROM Employees e " +
            "LEFT JOIN EmployeeRoles er ON e.emp_id = er.emp_id " +
            "LEFT JOIN Roles r ON er.role_id = r.role_id " +
            "ORDER BY e.lastName, e.firstName";
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<Employee> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapEmployee(rows[i]));
        }
        return list;
    }

    public Employee getEmployeeById(int empId) throws DLException {
        String sql =
            "SELECT e.emp_id, e.firstName, e.lastName, e.email, e.username, e.password_hash, " +
            "       COALESCE(r.role_name, 'Employee') AS role_name " +
            "FROM Employees e " +
            "LEFT JOIN EmployeeRoles er ON e.emp_id = er.emp_id " +
            "LEFT JOIN Roles r ON er.role_id = r.role_id " +
            "WHERE e.emp_id = ? LIMIT 1";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(empId));
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapEmployee(rows[1]) : null;
    }

    public Employee getEmployeeByUsername(String username) throws DLException {
        String sql =
            "SELECT e.emp_id, e.firstName, e.lastName, e.email, e.username, e.password_hash, " +
            "       COALESCE(r.role_name, 'Employee') AS role_name " +
            "FROM Employees e " +
            "LEFT JOIN EmployeeRoles er ON e.emp_id = er.emp_id " +
            "LEFT JOIN Roles r ON er.role_id = r.role_id " +
            "WHERE e.username = ? LIMIT 1";
        ArrayList<String> values = new ArrayList<>();
        values.add(username);
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapEmployee(rows[1]) : null;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public boolean saveEmployee(Employee employee) throws DLException {
        String sql = "INSERT INTO Employees (firstName, lastName, email, username, password_hash) VALUES (?, ?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(employee.getFirstName());
        values.add(employee.getLastName());
        values.add(employee.getEmail());
        values.add(employee.getUsername());
        values.add(employee.getPasswordHash());
        return database.setData(sql, values);
    }

    public boolean updateEmployee(Employee employee) throws DLException {
        String sql = "UPDATE Employees SET firstName = ?, lastName = ?, email = ? WHERE emp_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(employee.getFirstName());
        values.add(employee.getLastName());
        values.add(employee.getEmail());
        values.add(String.valueOf(employee.getEmpId()));
        return database.setData(sql, values);
    }

    public boolean updatePasswordHash(int empId, String newHash) throws DLException {
        String sql = "UPDATE Employees SET password_hash = ? WHERE emp_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(newHash);
        values.add(String.valueOf(empId));
        return database.setData(sql, values);
    }

    public boolean deleteEmployee(int empId) throws DLException {
        String sql = "DELETE FROM Employees WHERE emp_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(empId));
        return database.setData(sql, values);
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    /**
     * Column order from the SELECT:
     * emp_id, firstName, lastName, email, username, password_hash, role_name
     */
    private Employee mapEmployee(String[] row) {
        Employee e = new Employee();
        e.setEmpId(Integer.parseInt(row[0]));
        e.setFirstName(row[1]);
        e.setLastName(row[2]);
        e.setEmail(row[3]);
        e.setUsername(row[4]);
        e.setPasswordHash(row[5]);
        e.setRole(row[6]);
        return e;
    }
}
