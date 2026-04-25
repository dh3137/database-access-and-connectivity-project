package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;

import java.util.ArrayList;

public class EnquiryDatabase {

    private final MySQLDatabase db;

    public EnquiryDatabase(MySQLDatabase db) {
        this.db = db;
    }

    public boolean saveEnquiry(int vehicleId, int customerId, String name, String email, String phone, String message) throws DLException {
        boolean hasVehicle = vehicleId > 0;
        boolean hasCustomer = customerId > 0;

        String sql;
        ArrayList<String> params = new ArrayList<>();

        if (hasVehicle && hasCustomer) {
            sql = "INSERT INTO Enquiries (vehicle_id, customer_id, name, email, phone, message) VALUES (?, ?, ?, ?, ?, ?)";
            params.add(String.valueOf(vehicleId));
            params.add(String.valueOf(customerId));
        } else if (hasVehicle) {
            sql = "INSERT INTO Enquiries (vehicle_id, name, email, phone, message) VALUES (?, ?, ?, ?, ?)";
            params.add(String.valueOf(vehicleId));
        } else if (hasCustomer) {
            sql = "INSERT INTO Enquiries (customer_id, name, email, phone, message) VALUES (?, ?, ?, ?, ?)";
            params.add(String.valueOf(customerId));
        } else {
            sql = "INSERT INTO Enquiries (name, email, phone, message) VALUES (?, ?, ?, ?)";
        }

        params.add(name);
        params.add(email);
        params.add(phone != null ? phone : "");
        params.add(message != null ? message : "");

        return db.setData(sql, params);
    }

    public String[][] getRecent(int limit) throws DLException {
        String sql = """
            SELECT e.enquiry_id, e.name, e.email, e.phone, e.message, e.submitted_at, e.is_read,
                   COALESCE(CONCAT(m.model_name, ' (', v.year, ')'), 'General') AS vehicle_label,
                   e.customer_id, e.vehicle_id
            FROM Enquiries e
            LEFT JOIN Vehicles v ON e.vehicle_id = v.vehicle_id
            LEFT JOIN Models m ON v.model_id = m.model_id
            ORDER BY e.submitted_at DESC
            LIMIT ?
            """;
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(limit));
        return db.getData(sql, params);
    }

    public boolean markRead(int enquiryId, boolean read) throws DLException {
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(enquiryId));
        String sql = read
            ? "UPDATE Enquiries SET is_read = TRUE  WHERE enquiry_id = ?"
            : "UPDATE Enquiries SET is_read = FALSE WHERE enquiry_id = ?";
        return db.setData(sql, params);
    }
}
