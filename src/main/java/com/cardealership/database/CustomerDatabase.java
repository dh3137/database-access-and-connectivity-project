package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;
import java.sql.Connection;
import java.util.ArrayList;

public class CustomerDatabase {

    private final MySQLDatabase db;

    public CustomerDatabase(MySQLDatabase db) {
        this.db = db;
    }

    public int createCustomer(String firstName, String lastName, String email, String phone) throws DLException {
        String sql = "INSERT INTO Customers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
        ArrayList<String> params = new ArrayList<>();
        params.add(firstName);
        params.add(lastName);
        params.add(email);
        params.add(phone != null ? phone : "");
        return db.setDataReturnKey(sql, params);
    }

    public int createCustomer(Connection connection, String firstName, String lastName, String email, String phone) throws DLException {
        String sql = "INSERT INTO Customers (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";
        ArrayList<String> params = new ArrayList<>();
        params.add(firstName);
        params.add(lastName);
        params.add(email);
        params.add(phone != null ? phone : "");
        return db.setDataReturnKey(connection, sql, params);
    }

    public boolean emailExists(String email) throws DLException {
        String sql = "SELECT 1 FROM Customers WHERE email = ? LIMIT 1";
        ArrayList<String> params = new ArrayList<>();
        params.add(email);
        String[][] rows = db.getData(sql, params);
        return rows.length > 1;
    }

    public String[] getCustomerById(int customerId) throws DLException {
        String sql = "SELECT customer_id, first_name, last_name, email, phone FROM Customers WHERE customer_id = ?";
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(customerId));
        String[][] rows = db.getData(sql, params);
        return rows.length > 1 ? rows[1] : null;
    }

    public String[][] getAllCustomers() throws DLException {
        String sql = "SELECT customer_id, first_name, last_name, email FROM Customers ORDER BY last_name, first_name";
        return db.getData(sql, new ArrayList<>());
    }
}
