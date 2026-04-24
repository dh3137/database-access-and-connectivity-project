package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.VehicleChangeLog;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the VehicleChangeLog table.
 * Replaces the old ActionLogDatabase for vehicle-related audit events.
 */
public class VehicleChangeLogDatabase {

    private final MySQLDatabase database;

    public VehicleChangeLogDatabase(MySQLDatabase database) {
        this.database = database;
    }

    /**
     * Records a change event for a vehicle.
     *
     * @param vehicleId  the affected vehicle
     * @param empId      the employee who made the change
     * @param changeType short label, e.g. "INSERT", "UPDATE", "DELETE", "STATUS_CHANGE"
     */
    public boolean saveChangeLog(int vehicleId, int empId, String changeType) throws DLException {
        String sql = "INSERT INTO VehicleChangeLog (vehicle_id, emp_id, change_type, change_date) VALUES (?, ?, ?, NOW())";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicleId));
        values.add(String.valueOf(empId));
        values.add(changeType);
        return database.setData(sql, values);
    }

    public List<VehicleChangeLog> getRecentLogs(int limit) throws DLException {
        String sql = "SELECT log_id, vehicle_id, emp_id, change_type, change_date FROM VehicleChangeLog ORDER BY change_date DESC LIMIT " + limit;
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<VehicleChangeLog> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapLog(rows[i]));
        }
        return list;
    }

    public List<VehicleChangeLog> getLogsByVehicle(int vehicleId) throws DLException {
        String sql = "SELECT log_id, vehicle_id, emp_id, change_type, change_date FROM VehicleChangeLog WHERE vehicle_id = ? ORDER BY change_date DESC";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicleId));
        String[][] rows = database.getData(sql, values);
        List<VehicleChangeLog> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapLog(rows[i]));
        }
        return list;
    }

    private VehicleChangeLog mapLog(String[] row) {
        VehicleChangeLog log = new VehicleChangeLog();
        log.setLogId(Integer.parseInt(row[0]));
        log.setVehicleId(Integer.parseInt(row[1]));
        log.setEmpId(Integer.parseInt(row[2]));
        log.setChangeType(row[3]);
        log.setChangeDate(row[4]);
        return log;
    }
}
