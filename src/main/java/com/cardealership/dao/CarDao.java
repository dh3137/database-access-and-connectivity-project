package com.cardealership.dao;

import com.cardealership.model.Car;
import com.cardealership.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CarDao {

    public List<Car> findAll() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT id, make, model, year, price, status, color, mileage, image_url, description FROM cars ORDER BY id";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                cars.add(mapCar(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load cars from the database.", e);
        }

        return cars;
    }

    public Car findById(int id) {
        String sql = "SELECT id, make, model, year, price, status, color, mileage, image_url, description FROM cars WHERE id = ?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapCar(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load the selected car.", e);
        }

        return null;
    }

    public boolean addCar(Car car) {
        String sql = "INSERT INTO cars (make, model, year, price, status, color, mileage, image_url, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, car.getMake());
            statement.setString(2, car.getModel());
            statement.setInt(3, car.getYear());
            statement.setDouble(4, car.getPrice());
            statement.setString(5, car.getStatus() != null ? car.getStatus() : "AVAILABLE");
            statement.setString(6, car.getColor());
            statement.setInt(7, car.getMileage());
            statement.setString(8, car.getImageUrl());
            statement.setString(9, car.getDescription());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not save the car.", e);
        }
    }

    public boolean updateCar(Car car) {
        String sql = "UPDATE cars SET make=?, model=?, year=?, price=?, status=?, color=?, mileage=?, image_url=?, description=? WHERE id=?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, car.getMake());
            statement.setString(2, car.getModel());
            statement.setInt(3, car.getYear());
            statement.setDouble(4, car.getPrice());
            statement.setString(5, car.getStatus() != null ? car.getStatus() : "AVAILABLE");
            statement.setString(6, car.getColor());
            statement.setInt(7, car.getMileage());
            statement.setString(8, car.getImageUrl());
            statement.setString(9, car.getDescription());
            statement.setInt(10, car.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not update the car.", e);
        }
    }

    public boolean deleteCar(int id) {
        String sql = "DELETE FROM cars WHERE id=?";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete the car.", e);
        }
    }

    private Connection openConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    private Car mapCar(ResultSet rs) throws SQLException {
        Car car = new Car();
        car.setId(rs.getInt("id"));
        car.setMake(rs.getString("make"));
        car.setModel(rs.getString("model"));
        car.setYear(rs.getInt("year"));
        car.setPrice(rs.getDouble("price"));
        car.setStatus(rs.getString("status"));
        car.setColor(rs.getString("color"));
        car.setMileage(rs.getInt("mileage"));
        car.setImageUrl(rs.getString("image_url"));
        car.setDescription(rs.getString("description"));
        return car;
    }
}
