package com.cardealership.model;

/**
 * Represents a row in the Reservations table.
 */
public class Reservation {

    private int reservationId;
    private int vehicleId;
    private int customerId;
    private String reservationDate; // yyyy-MM-dd

    public Reservation() {}

    public Reservation(int reservationId, int vehicleId, int customerId, String reservationDate) {
        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.reservationDate = reservationDate;
    }

    public int getReservationId()                          { return reservationId; }
    public void setReservationId(int id)                   { this.reservationId = id; }

    public int getVehicleId()                              { return vehicleId; }
    public void setVehicleId(int vehicleId)                { this.vehicleId = vehicleId; }

    public int getCustomerId()                             { return customerId; }
    public void setCustomerId(int customerId)              { this.customerId = customerId; }

    public String getReservationDate()                     { return reservationDate; }
    public void setReservationDate(String reservationDate) { this.reservationDate = reservationDate; }
}
