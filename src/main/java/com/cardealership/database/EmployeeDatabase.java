package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;
import java.util.ArrayList;

public class EmployeeDatabase {

    private final MySQLDatabase db;

    public EmployeeDatabase(MySQLDatabase db) {
        this.db = db;
    }

    public String[][] getAllEmployees() throws DLException {
        String sql =
            "SELECT e.emp_id, e.first_name, e.last_name, e.email, e.phone, e.hire_date, e.is_active, " +
            "u.username, COALESCE(u.role, 'EMPLOYEE') AS account_role " +
            "FROM Employees e " +
            "LEFT JOIN Users u ON u.emp_id = e.emp_id " +
            "ORDER BY e.is_active DESC, e.last_name, e.first_name";
        return db.getData(sql, new ArrayList<>());
    }
}
