package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.Reservation;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the Reservations table.
 */
public class ReservationDatabase {

    private final MySQLDatabase database;

    public ReservationDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<Reservation> getAllReservations() throws DLException {
        String sql = "SELECT reservation_id, vehicle_id, customer_id, reservation_date FROM Reservations ORDER BY reservation_date DESC";
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<Reservation> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapReservation(rows[i]));
        }
        return list;
    }

    public Reservation getReservationById(int id) throws DLException {
        String sql = "SELECT reservation_id, vehicle_id, customer_id, reservation_date FROM Reservations WHERE reservation_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapReservation(rows[1]) : null;
    }

    public boolean saveReservation(Reservation reservation) throws DLException {
        String sql = "INSERT INTO Reservations (vehicle_id, customer_id, reservation_date) VALUES (?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(reservation.getVehicleId()));
        values.add(String.valueOf(reservation.getCustomerId()));
        values.add(reservation.getReservationDate());
        return database.setData(sql, values);
    }

    public boolean deleteReservation(int id) throws DLException {
        String sql = "DELETE FROM Reservations WHERE reservation_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        return database.setData(sql, values);
    }

    private Reservation mapReservation(String[] row) {
        Reservation r = new Reservation();
        r.setReservationId(Integer.parseInt(row[0]));
        r.setVehicleId(Integer.parseInt(row[1]));
        r.setCustomerId(Integer.parseInt(row[2]));
        r.setReservationDate(row[3]);
        return r;
    }
}
