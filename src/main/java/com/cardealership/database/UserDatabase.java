package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.model.User;
import com.cardealership.util.MySQLDatabase;
import java.util.ArrayList;

public class UserDatabase {

    private final MySQLDatabase database;

    public UserDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public User authenticate(String username, String password) throws DLException {
        String sql = "SELECT id, username, password, role, full_name FROM users WHERE username = ? AND password = ?";
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
        String sql = "SELECT id, username, password, role, full_name FROM users WHERE username = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(username);
        String[][] rows = database.getData(sql, values);

        if (rows.length > 1) {
            return mapUser(rows[1]);
        }

        return null;
    }

    public boolean saveUser(User user) throws DLException {
        String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(user.getUsername());
        values.add(user.getPassword());
        values.add(user.getRole());
        values.add(user.getFullName());
        return database.setData(sql, values);
    }

    private User mapUser(String[] row) {
        User user = new User();
        user.setId(Integer.parseInt(row[0]));
        user.setUsername(row[1]);
        user.setPassword(row[2]);
        user.setRole(row[3]);
        user.setFullName(row[4]);
        return user;
    }
}
