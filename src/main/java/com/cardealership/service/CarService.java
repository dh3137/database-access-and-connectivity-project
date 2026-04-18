package com.cardealership.service;

import com.cardealership.dao.CarDao;
import com.cardealership.model.Car;
import java.time.Year;
import java.util.List;

public class CarService {

    private final CarDao carDao;

    public CarService() {
        this.carDao = new CarDao();
    }

    public List<Car> getAllCars() {
        return carDao.findAll();
    }

    public Car getCarById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Car id must be greater than 0.");
        }

        return carDao.findById(id);
    }

    public boolean addCar(Car car) {
        validateCar(car);
        return carDao.addCar(car);
    }

    public boolean updateCar(Car car) {
        if (car.getId() <= 0) {
            throw new IllegalArgumentException("Car id is required for update.");
        }
        validateCar(car);
        return carDao.updateCar(car);
    }

    public boolean deleteCar(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Car id must be greater than 0.");
        }
        return carDao.deleteCar(id);
    }

    private void validateCar(Car car) {
        int maxYear = Year.now().getValue() + 1;

        if (car == null) {
            throw new IllegalArgumentException("Car data is required.");
        }

        if (car.getMake() == null || car.getMake().trim().isEmpty()) {
            throw new IllegalArgumentException("Car make is required.");
        }

        if (car.getModel() == null || car.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Car model is required.");
        }

        if (car.getYear() < 1900 || car.getYear() > maxYear) {
            throw new IllegalArgumentException("Car year is not valid.");
        }

        if (car.getPrice() < 0) {
            throw new IllegalArgumentException("Car price cannot be negative.");
        }
    }
}
