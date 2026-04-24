package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.Vehicle;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the Vehicles table.
 *
 * Updated for Issue 1: includes joins to Models, Manufacturers, AND VehicleImages.
 * Also supports the restored year, color, mileage, and description fields.
 */
public class VehicleDatabase {

    private final MySQLDatabase database;

    public VehicleDatabase(MySQLDatabase database) {
        this.database = database;
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    private final String SELECT_BASE =
        "SELECT v.vehicle_id, v.vin, v.model_id, v.year, v.price, v.status, v.color, v.mileage, v.description, " +
        "       m.model_name, mfr.name AS manufacturer_name, img.image_url " +
        "FROM Vehicles v " +
        "JOIN Models m   ON v.model_id = m.model_id " +
        "JOIN Manufacturers mfr ON m.manufacturer_id = mfr.manufacturer_id " +
        "LEFT JOIN VehicleImages img ON v.vehicle_id = img.vehicle_id ";

    public List<Vehicle> getAllVehicles() throws DLException {
        String sql = SELECT_BASE + "ORDER BY v.vehicle_id";
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<Vehicle> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapVehicle(rows[i]));
        }
        return list;
    }

    public Vehicle getVehicleById(int vehicleId) throws DLException {
        String sql = SELECT_BASE + "WHERE v.vehicle_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicleId));
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapVehicle(rows[1]) : null;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public boolean saveVehicle(Vehicle vehicle) throws DLException {
        String sql = "INSERT INTO Vehicles (vin, model_id, year, price, status, color, mileage, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(vehicle.getVin());
        values.add(String.valueOf(vehicle.getModelId()));
        values.add(String.valueOf(vehicle.getYear()));
        values.add(String.valueOf(vehicle.getPrice()));
        values.add(vehicle.getStatus() != null ? vehicle.getStatus() : "Available");
        values.add(vehicle.getColor());
        values.add(String.valueOf(vehicle.getMileage()));
        values.add(vehicle.getDescription());
        return database.setData(sql, values);
    }

    public boolean updateVehicle(Vehicle vehicle) throws DLException {
        String sql = "UPDATE Vehicles SET year = ?, price = ?, status = ?, color = ?, mileage = ?, description = ? WHERE vehicle_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicle.getYear()));
        values.add(String.valueOf(vehicle.getPrice()));
        values.add(vehicle.getStatus() != null ? vehicle.getStatus() : "Available");
        values.add(vehicle.getColor());
        values.add(String.valueOf(vehicle.getMileage()));
        values.add(vehicle.getDescription());
        values.add(String.valueOf(vehicle.getVehicleId()));
        return database.setData(sql, values);
    }

    public boolean deleteVehicle(int vehicleId) throws DLException {
        String sql = "DELETE FROM Vehicles WHERE vehicle_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicleId));
        return database.setData(sql, values);
    }

    public boolean saveImage(int vehicleId, String url) throws DLException {
        // Remove any existing images for this vehicle first to ensure we only have one "locked in" image
        database.setData("DELETE FROM VehicleImages WHERE vehicle_id = ?", new ArrayList<>(List.of(String.valueOf(vehicleId))));
        String sql = "INSERT INTO VehicleImages (vehicle_id, image_url) VALUES (?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(vehicleId));
        values.add(url);
        return database.setData(sql, values);
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    /**
     * Maps raw result row to Vehicle object.
     * Columns: id, vin, model_id, year, price, status, color, mileage, desc, model_name, manufacturer_name, image_url
     */
    private Vehicle mapVehicle(String[] row) {
        Vehicle v = new Vehicle();
        v.setVehicleId(Integer.parseInt(row[0]));
        v.setVin(row[1]);
        v.setModelId(Integer.parseInt(row[2]));
        v.setYear(Integer.parseInt(row[3]));
        v.setPrice(Double.parseDouble(row[4]));
        v.setStatus(row[5]);
        v.setColor(row[6]);
        v.setMileage(row[7] != null ? Integer.parseInt(row[7]) : 0);
        v.setDescription(row[8]);
        v.setModelName(row[9]);
        v.setManufacturerName(row[10]);
        v.setImageUrl(row[11]);
        return v;
    }
}
