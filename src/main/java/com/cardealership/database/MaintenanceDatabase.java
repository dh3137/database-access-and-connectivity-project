package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;
import java.util.ArrayList;

public class MaintenanceDatabase {

    private final MySQLDatabase db;

    public MaintenanceDatabase(MySQLDatabase db) {
        this.db = db;
    }

    // Returns header row + data rows; columns: maintenance_id(0) service_date(1) service_type(2) description(3) cost(4) performed_by(5)
    public String[][] getByVehicleId(int vehicleId) throws DLException {
        String sql = """
            SELECT maintenance_id, service_date, service_type, description, cost, performed_by
            FROM MaintenanceHistory
            WHERE vehicle_id = ?
            ORDER BY service_date DESC
            """;
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(vehicleId));
        return db.getData(sql, params);
    }

    public boolean addRecord(int vehicleId, String serviceDate, String serviceType,
                             String description, String cost, String performedBy) throws DLException {
        String sql = """
            INSERT INTO MaintenanceHistory (vehicle_id, service_date, service_type, description, cost, performed_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(vehicleId));
        params.add(serviceDate);
        params.add(serviceType);
        params.add(description != null ? description : "");
        params.add(cost != null && !cost.isBlank() ? cost : null);
        params.add(performedBy != null ? performedBy : "");
        return db.setData(sql, params);
    }
}
