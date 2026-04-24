package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;

import java.util.ArrayList;

public class EnquiryDatabase {

    private final MySQLDatabase db;

    public EnquiryDatabase(MySQLDatabase db) {
        this.db = db;
    }

    public boolean saveEnquiry(int vehicleId, String name, String email, String phone, String message) throws DLException {
        String sql = vehicleId > 0
            ? "INSERT INTO Enquiries (vehicle_id, name, email, phone, message) VALUES (?, ?, ?, ?, ?)"
            : "INSERT INTO Enquiries (vehicle_id, name, email, phone, message) VALUES (NULL, ?, ?, ?, ?)";

        ArrayList<String> params = new ArrayList<>();
        if (vehicleId > 0) params.add(String.valueOf(vehicleId));
        params.add(name);
        params.add(email);
        params.add(phone != null ? phone : "");
        params.add(message != null ? message : "");

        return db.setData(sql, params);
    }

    public String[][] getRecent(int limit) throws DLException {
        String sql = """
            SELECT e.enquiry_id, e.name, e.email, e.phone, e.message, e.submitted_at, e.is_read,
                   COALESCE(CONCAT(m.model_name, ' (', v.year, ')'), 'General') AS vehicle_label
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

    public boolean markRead(int enquiryId) throws DLException {
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(enquiryId));
        return db.setData("UPDATE Enquiries SET is_read = TRUE WHERE enquiry_id = ?", params);
    }
}
