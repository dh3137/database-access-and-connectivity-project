package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.Car;
import com.cardealership.util.MySQLDatabase;

public class CarDatabase {

    private static final String SELECT_COLS =
        "v.vehicle_id, mfr.name, mo.model_name, v.year, v.price, v.status, " +
        "v.color, v.mileage, vi.image_url, v.description, v.vin, " +
        "mo.model_id, mo.manufacturer_id, mo.segment";

    private static final String FROM_JOINS =
        "FROM Vehicles v " +
        "JOIN Models mo ON v.model_id = mo.model_id " +
        "JOIN Manufacturers mfr ON mo.manufacturer_id = mfr.manufacturer_id " +
        "LEFT JOIN VehicleImages vi ON vi.vehicle_id = v.vehicle_id AND vi.is_primary = TRUE";

    private final MySQLDatabase database;

    public CarDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<Car> getAllCars() throws DLException {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLS + " " + FROM_JOINS + " ORDER BY v.vehicle_id";
        String[][] rows = database.getData(sql, new ArrayList<>());

        for (int i = 1; i < rows.length; i++) {
            cars.add(mapCar(rows[i]));
        }

        return cars;
    }

    public List<Car> getAvailableCars() throws DLException {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLS + " " + FROM_JOINS + " WHERE v.status = 'Available' ORDER BY v.vehicle_id";
        String[][] rows = database.getData(sql, new ArrayList<>());

        for (int i = 1; i < rows.length; i++) {
            cars.add(mapCar(rows[i]));
        }

        return cars;
    }

    public Car getCarById(int id) throws DLException {
        String sql = "SELECT " + SELECT_COLS + " " + FROM_JOINS + " WHERE v.vehicle_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        String[][] rows = database.getData(sql, values);

        if (rows.length > 1) {
            return mapCar(rows[1]);
        }

        return null;
    }

    public boolean saveCar(Car car) throws DLException {
        String sql = "INSERT INTO Vehicles (model_id, year, price, status, color, mileage, vin, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(car.getModelId()));
        values.add(String.valueOf(car.getYear()));
        values.add(String.valueOf(car.getPrice()));
        values.add(car.getStatus() != null ? car.getStatus() : "Available");
        values.add(valueOrEmpty(car.getColor()));
        values.add(String.valueOf(car.getMileage()));
        values.add(valueOrEmpty(car.getVin()));
        values.add(valueOrEmpty(car.getDescription()));
        return database.setData(sql, values);
    }

    public boolean updateCar(Car car) throws DLException {
        String sql = "UPDATE Vehicles SET model_id=?, year=?, price=?, status=?, color=?, mileage=?, vin=?, description=? WHERE vehicle_id=?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(car.getModelId()));
        values.add(String.valueOf(car.getYear()));
        values.add(String.valueOf(car.getPrice()));
        values.add(car.getStatus() != null ? car.getStatus() : "Available");
        values.add(valueOrEmpty(car.getColor()));
        values.add(String.valueOf(car.getMileage()));
        values.add(valueOrEmpty(car.getVin()));
        values.add(valueOrEmpty(car.getDescription()));
        values.add(String.valueOf(car.getId()));
        return database.setData(sql, values);
    }

    public boolean deleteCar(int id) throws DLException {
        String sql = "DELETE FROM Vehicles WHERE vehicle_id=?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        return database.setData(sql, values);
    }

    private Car mapCar(String[] row) {
        Car car = new Car();
        car.setId(parseInt(row[0]));
        car.setMake(row[1]);
        car.setModel(row[2]);
        car.setYear(parseInt(row[3]));
        car.setPrice(row[4] != null && !row[4].isBlank() ? Double.parseDouble(row[4]) : 0.0);
        car.setStatus(row[5]);
        car.setColor(row[6]);
        car.setMileage(parseInt(row[7]));
        car.setImageUrl(row[8]);
        car.setDescription(row[9]);
        car.setVin(row[10]);
        car.setModelId(parseInt(row[11]));
        car.setManufacturerId(parseInt(row[12]));
        car.setSegment(row[13]);
        return car;
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) return 0;
        return Integer.parseInt(value);
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
