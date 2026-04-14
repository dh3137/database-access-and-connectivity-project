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
        String sql = "SELECT id, make, model, year, price FROM cars ORDER BY id";

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
        String sql = "SELECT id, make, model, year, price FROM cars WHERE id = ?";

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
        String sql = "INSERT INTO cars (make, model, year, price) VALUES (?, ?, ?, ?)";

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, car.getMake());
            statement.setString(2, car.getModel());
            statement.setInt(3, car.getYear());
            statement.setDouble(4, car.getPrice());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not save the car.", e);
        }
    }

    private Connection openConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    private Car mapCar(ResultSet resultSet) throws SQLException {
        Car car = new Car();
        car.setId(resultSet.getInt("id"));
        car.setMake(resultSet.getString("make"));
        car.setModel(resultSet.getString("model"));
        car.setYear(resultSet.getInt("year"));
        car.setPrice(resultSet.getDouble("price"));
        return car;
    }
}
