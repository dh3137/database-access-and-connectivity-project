package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.MaintenanceHistory;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the MaintenanceHistory table.
 */
public class MaintenanceDatabase {

    private final MySQLDatabase database;

    public MaintenanceDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<MaintenanceHistory> getByVehicle(int vehicleId) throws DLException {
        String sql = "SELECT maintenance_id, vehicle_id, service_date, description FROM MaintenanceHistory WHERE vehicle_id = ? ORDER BY service_date DESC";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicleId));
        String[][] rows = database.getData(sql, values);
        List<MaintenanceHistory> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapMaintenance(rows[i]));
        }
        return list;
    }

    public List<MaintenanceHistory> getAllMaintenance() throws DLException {
        String sql = "SELECT maintenance_id, vehicle_id, service_date, description FROM MaintenanceHistory ORDER BY service_date DESC";
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<MaintenanceHistory> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapMaintenance(rows[i]));
        }
        return list;
    }

    public boolean saveMaintenance(MaintenanceHistory record) throws DLException {
        String sql = "INSERT INTO MaintenanceHistory (vehicle_id, service_date, description) VALUES (?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(record.getVehicleId()));
        values.add(record.getServiceDate());
        values.add(record.getDescription());
        return database.setData(sql, values);
    }

    public boolean deleteMaintenance(int maintenanceId) throws DLException {
        String sql = "DELETE FROM MaintenanceHistory WHERE maintenance_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(maintenanceId));
        return database.setData(sql, values);
    }

    private MaintenanceHistory mapMaintenance(String[] row) {
        MaintenanceHistory m = new MaintenanceHistory();
        m.setMaintenanceId(Integer.parseInt(row[0]));
        m.setVehicleId(Integer.parseInt(row[1]));
        m.setServiceDate(row[2]);
        m.setDescription(row[3]);
        return m;
    }
}
