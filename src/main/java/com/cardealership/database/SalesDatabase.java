package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;

import java.util.ArrayList;

public class SalesDatabase {

    private final MySQLDatabase db;

    public SalesDatabase(MySQLDatabase db) {
        this.db = db;
    }

    public boolean recordSale(int vehicleId, int customerId, int empId, double salePrice, String paymentMethod, String notes) throws DLException {
        String sql = empId > 0
            ? "INSERT INTO Sales (vehicle_id, customer_id, emp_id, sale_price, payment_method, notes) VALUES (?, ?, ?, ?, ?, ?)"
            : "INSERT INTO Sales (vehicle_id, customer_id, emp_id, sale_price, payment_method, notes) VALUES (?, ?, NULL, ?, ?, ?)";

        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(vehicleId));
        params.add(String.valueOf(customerId));
        if (empId > 0) params.add(String.valueOf(empId));
        params.add(String.valueOf(salePrice));
        params.add(paymentMethod != null ? paymentMethod : "CASH");
        params.add(notes != null ? notes : "");

        boolean inserted = db.setData(sql, params);
        if (!inserted) return false;

        ArrayList<String> statusParams = new ArrayList<>();
        statusParams.add(String.valueOf(vehicleId));
        return db.setData("UPDATE Vehicles SET status = 'Sold' WHERE vehicle_id = ?", statusParams);
    }

    public String[][] getRecentSales(int limit) throws DLException {
        String sql = """
            SELECT s.sale_id, s.sale_price, s.payment_method, s.sale_date, s.notes,
                   CONCAT(m.model_name, ' (', v.year, ')') AS vehicle_label,
                   CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
                   c.email AS customer_email
            FROM Sales s
            JOIN Vehicles v ON s.vehicle_id = v.vehicle_id
            JOIN Models m ON v.model_id = m.model_id
            JOIN Customers c ON s.customer_id = c.customer_id
            ORDER BY s.sale_date DESC
            LIMIT ?
            """;
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(limit));
        return db.getData(sql, params);
    }
}
