package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.model.Car;
import com.cardealership.util.MySQLDatabase;
import java.util.ArrayList;
import java.util.List;

public class CarDatabase {

    private final MySQLDatabase database;

    public CarDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<Car> getAllCars() throws DLException {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT id, make, model, year, price, status, color, mileage, image_url, description FROM cars ORDER BY id";
        String[][] rows = database.getData(sql, new ArrayList<>());

        for (int i = 1; i < rows.length; i++) {
            cars.add(mapCar(rows[i]));
        }

        return cars;
    }

    public Car getCarById(int id) throws DLException {
        String sql = "SELECT id, make, model, year, price, status, color, mileage, image_url, description FROM cars WHERE id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        String[][] rows = database.getData(sql, values);

        if (rows.length > 1) {
            return mapCar(rows[1]);
        }

        return null;
    }

    public boolean saveCar(Car car) throws DLException {
        String sql = "INSERT INTO cars (make, model, year, price, status, color, mileage, image_url, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(car.getMake());
        values.add(car.getModel());
        values.add(String.valueOf(car.getYear()));
        values.add(String.valueOf(car.getPrice()));
        values.add(car.getStatus() != null ? car.getStatus() : "AVAILABLE");
        values.add(valueOrEmpty(car.getColor()));
        values.add(String.valueOf(car.getMileage()));
        values.add(valueOrEmpty(car.getImageUrl()));
        values.add(valueOrEmpty(car.getDescription()));
        return database.setData(sql, values);
    }

    public boolean updateCar(Car car) throws DLException {
        String sql = "UPDATE cars SET make=?, model=?, year=?, price=?, status=?, color=?, mileage=?, image_url=?, description=? WHERE id=?";
        ArrayList<String> values = new ArrayList<>();
        values.add(car.getMake());
        values.add(car.getModel());
        values.add(String.valueOf(car.getYear()));
        values.add(String.valueOf(car.getPrice()));
        values.add(car.getStatus() != null ? car.getStatus() : "AVAILABLE");
        values.add(valueOrEmpty(car.getColor()));
        values.add(String.valueOf(car.getMileage()));
        values.add(valueOrEmpty(car.getImageUrl()));
        values.add(valueOrEmpty(car.getDescription()));
        values.add(String.valueOf(car.getId()));
        return database.setData(sql, values);
    }

    public boolean deleteCar(int id) throws DLException {
        String sql = "DELETE FROM cars WHERE id=?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        return database.setData(sql, values);
    }

    private Car mapCar(String[] row) {
        Car car = new Car();
        car.setId(Integer.parseInt(row[0]));
        car.setMake(row[1]);
        car.setModel(row[2]);
        car.setYear(Integer.parseInt(row[3]));
        car.setPrice(Double.parseDouble(row[4]));
        car.setStatus(row[5]);
        car.setColor(row[6]);
        car.setMileage(parseInt(row[7]));
        car.setImageUrl(row[8]);
        car.setDescription(row[9]);
        return car;
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
