package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.model.User;
import com.cardealership.util.MySQLDatabase;
import java.sql.Connection;
import java.util.ArrayList;

public class UserDatabase {

    private final MySQLDatabase database;

    public UserDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public User authenticate(String username, String password) throws DLException {
        String sql = "SELECT user_id, username, password, role, emp_id, customer_id, created_at FROM Users WHERE username = ? AND password = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(username);
        values.add(password);
        String[][] rows = database.getData(sql, values);

        if (rows.length > 1) {
            return mapUser(rows[1]);
        }

        return null;
    }

    public User getUserByUsername(String username) throws DLException {
        String sql = "SELECT user_id, username, password, role, emp_id, customer_id, created_at FROM Users WHERE username = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(username);
        String[][] rows = database.getData(sql, values);

        if (rows.length > 1) {
            return mapUser(rows[1]);
        }

        return null;
    }

    public boolean saveUser(User user) throws DLException {
        return saveUser(null, user);
    }

    public boolean saveUser(Connection connection, User user) throws DLException {
        if (connection == null) {
            return saveUserStandalone(user);
        }
        return saveUserWithConnection(connection, user);
    }

    private boolean saveUserStandalone(User user) throws DLException {
        if (user.getEmpId() > 0) {
            String sql = "INSERT INTO Users (username, password, role, emp_id) VALUES (?, ?, ?, ?)";
            ArrayList<String> values = new ArrayList<>();
            values.add(user.getUsername());
            values.add(user.getPassword());
            values.add(user.getRole());
            values.add(String.valueOf(user.getEmpId()));
            return database.setData(sql, values);
        } else if (user.getCustomerId() > 0) {
            String sql = "INSERT INTO Users (username, password, role, customer_id) VALUES (?, ?, ?, ?)";
            ArrayList<String> values = new ArrayList<>();
            values.add(user.getUsername());
            values.add(user.getPassword());
            values.add(user.getRole());
            values.add(String.valueOf(user.getCustomerId()));
            return database.setData(sql, values);
        } else {
            String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";
            ArrayList<String> values = new ArrayList<>();
            values.add(user.getUsername());
            values.add(user.getPassword());
            values.add(user.getRole());
            return database.setData(sql, values);
        }
    }

    private boolean saveUserWithConnection(Connection connection, User user) throws DLException {
        if (user.getEmpId() > 0) {
            String sql = "INSERT INTO Users (username, password, role, emp_id) VALUES (?, ?, ?, ?)";
            ArrayList<String> values = new ArrayList<>();
            values.add(user.getUsername());
            values.add(user.getPassword());
            values.add(user.getRole());
            values.add(String.valueOf(user.getEmpId()));
            return database.setData(connection, sql, values);
        } else if (user.getCustomerId() > 0) {
            String sql = "INSERT INTO Users (username, password, role, customer_id) VALUES (?, ?, ?, ?)";
            ArrayList<String> values = new ArrayList<>();
            values.add(user.getUsername());
            values.add(user.getPassword());
            values.add(user.getRole());
            values.add(String.valueOf(user.getCustomerId()));
            return database.setData(connection, sql, values);
        } else {
            String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";
            ArrayList<String> values = new ArrayList<>();
            values.add(user.getUsername());
            values.add(user.getPassword());
            values.add(user.getRole());
            return database.setData(connection, sql, values);
        }
    }

    private User mapUser(String[] row) {
        User user = new User();
        user.setId(parseInt(row[0]));
        user.setUsername(row[1]);
        user.setPassword(row[2]);
        user.setRole(row[3]);
        user.setEmpId(parseInt(row[4]));
        user.setCustomerId(parseInt(row[5]));
        user.setCreatedAt(row[6]);
        return user;
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) return 0;
        return Integer.parseInt(value);
    }
}
