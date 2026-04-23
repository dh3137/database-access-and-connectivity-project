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

    public List<String[]> getRecent(int limit) throws DLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT vcl.vehicle_id, e.first_name, e.last_name, vcl.change_type, vcl.field_changed, vcl.new_value, vcl.change_date " +
                     "FROM VehicleChangeLog vcl " +
                     "JOIN Employees e ON vcl.emp_id = e.emp_id " +
                     "ORDER BY vcl.change_date DESC LIMIT ?";
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
