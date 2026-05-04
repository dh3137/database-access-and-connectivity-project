package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;

public class ActionLogDatabase {

    private final MySQLDatabase database;

    public ActionLogDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public boolean saveActionLog(int vehicleId, int empId, String changeType, String fieldChanged, String newValue) throws DLException {
        String sql = "INSERT INTO VehicleChangeLog (vehicle_id, emp_id, change_type, field_changed, new_value) VALUES (?, ?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicleId));
        values.add(String.valueOf(empId));
        values.add(changeType != null ? changeType : "UPDATE");
        values.add(fieldChanged != null ? fieldChanged : "");
        values.add(newValue != null ? newValue : "");
        return database.setData(sql, values);
    }

    public boolean saveGeneralAction(int userId, int empId, String actionType, String objectType, String objectId, String details) throws DLException {
        String sql = "INSERT INTO ActionLog (user_id, emp_id, action_type, object_type, object_id, details) VALUES (?, ?, ?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(userId > 0 ? String.valueOf(userId) : null);
        values.add(empId > 0 ? String.valueOf(empId) : null);
        values.add(actionType != null ? actionType : "ACTION");
        values.add(objectType != null ? objectType : "");
        values.add(objectId != null ? objectId : "");
        values.add(details != null ? details : "");
        return database.setData(sql, values);
    }

    public List<String[]> getRecent(int limit) throws DLException {
        List<String[]> rows = new ArrayList<>();
        String sql = """
            SELECT *
            FROM (
                SELECT
                    CAST(vcl.vehicle_id AS CHAR) AS object_id,
                    e.first_name AS first_name,
                    e.last_name AS last_name,
                    vcl.change_type AS action_type,
                    COALESCE(vcl.field_changed, '') AS field_changed,
                    COALESCE(vcl.new_value, '') AS details,
                    vcl.change_date AS action_time
                FROM VehicleChangeLog vcl
                JOIN Employees e ON vcl.emp_id = e.emp_id
                UNION ALL
                SELECT
                    COALESCE(al.object_id, '') AS object_id,
                    COALESCE(e.first_name, 'System') AS first_name,
                    COALESCE(e.last_name, '') AS last_name,
                    al.action_type AS action_type,
                    COALESCE(al.object_type, '') AS field_changed,
                    COALESCE(al.details, '') AS details,
                    al.action_date AS action_time
                FROM ActionLog al
                LEFT JOIN Employees e ON al.emp_id = e.emp_id
            ) activity
            ORDER BY action_time DESC
            LIMIT ?
            """;
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(limit));
        String[][] result = database.getData(sql, values);

        for (int i = 1; i < result.length; i++) {
            rows.add(result[i]);
        }

        return rows;
    }

    public List<String[]> getByVehicle(int vehicleId) throws DLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT vcl.vehicle_id, e.first_name, e.last_name, vcl.change_type, vcl.field_changed, vcl.new_value, vcl.change_date " +
                     "FROM VehicleChangeLog vcl " +
                     "JOIN Employees e ON vcl.emp_id = e.emp_id " +
                     "WHERE vcl.vehicle_id = ? " +
                     "ORDER BY vcl.change_date DESC";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicleId));
        String[][] result = database.getData(sql, values);

        for (int i = 1; i < result.length; i++) {
            rows.add(result[i]);
        }

        return rows;
    }
}
