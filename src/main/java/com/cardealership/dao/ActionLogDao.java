package com.cardealership.dao;

import com.cardealership.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActionLogDao {

    public void log(String username, String action, String detail) {
        String sql = "INSERT INTO action_log (username, action, detail) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, action);
            ps.setString(3, detail);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[action_log] Failed to write log: " + e.getMessage());
        }
    }

    public List<String[]> getRecent(int limit) {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT username, action, detail, created_at FROM action_log ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("detail"),
                        rs.getString("created_at")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[action_log] Failed to read log: " + e.getMessage());
        }
        return rows;
    }
}
