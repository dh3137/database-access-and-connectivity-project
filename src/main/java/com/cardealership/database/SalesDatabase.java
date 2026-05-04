package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class SalesDatabase {

    private final MySQLDatabase db;

    public SalesDatabase(MySQLDatabase db) {
        this.db = db;
    }

    public boolean recordSale(int vehicleId, int customerId, int empId, double salePrice, String paymentMethod, String notes) throws DLException {
        String saleSql = "INSERT INTO Sales (vehicle_id, customer_id, emp_id, sale_price, payment_method, notes) VALUES (?, ?, ?, ?, ?, ?)";
        String statusSql = "UPDATE Vehicles SET status = 'Sold' WHERE vehicle_id = ? AND status <> 'Sold'";
        String reservationSql = "UPDATE Reservations SET status = 'Converted' WHERE vehicle_id = ? AND status = 'Active'";

        try (Connection connection = db.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                if (!rowExists(connection, "SELECT 1 FROM Customers WHERE customer_id = ? LIMIT 1", customerId)
                        || !rowExists(connection, "SELECT 1 FROM Employees WHERE emp_id = ? AND is_active = TRUE LIMIT 1", empId)) {
                    connection.rollback();
                    return false;
                }

                ArrayList<String> statusParams = new ArrayList<>();
                statusParams.add(String.valueOf(vehicleId));
                try (PreparedStatement statusStatement = db.prepare(connection, statusSql, statusParams)) {
                    int updatedRows = statusStatement.executeUpdate();
                    if (updatedRows != 1) {
                        connection.rollback();
                        return false;
                    }
                }

                ArrayList<String> saleParams = new ArrayList<>();
                saleParams.add(String.valueOf(vehicleId));
                saleParams.add(String.valueOf(customerId));
                saleParams.add(String.valueOf(empId));
                saleParams.add(String.valueOf(salePrice));
                saleParams.add(paymentMethod != null ? paymentMethod : "CASH");
                saleParams.add(notes != null ? notes : "");

                try (PreparedStatement saleStatement = db.prepare(connection, saleSql, saleParams)) {
                    saleStatement.executeUpdate();
                }

                ArrayList<String> reservationParams = new ArrayList<>();
                reservationParams.add(String.valueOf(vehicleId));
                try (PreparedStatement reservationStatement = db.prepare(connection, reservationSql, reservationParams)) {
                    reservationStatement.executeUpdate();
                }

                connection.commit();
                return true;
            } catch (DLException | SQLException e) {
                rollbackQuietly(connection);
                if (e instanceof DLException dlException) {
                    throw dlException;
                }
                throw new DLException((SQLException) e, "Operation=recordSale", "DatabaseType=MySQL");
            } finally {
                restoreAutoCommit(connection, originalAutoCommit);
            }
        } catch (DLException e) {
            throw e;
        } catch (SQLException e) {
            throw new DLException(e, "Operation=recordSale", "DatabaseType=MySQL");
        }
    }

    private boolean rowExists(Connection connection, String sql, int id) throws DLException, SQLException {
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(id));
        try (PreparedStatement statement = db.prepare(connection, sql, params);
             java.sql.ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            System.err.println("[sales] Rollback failed: " + rollbackError.getMessage());
        }
    }

    private void restoreAutoCommit(Connection connection, boolean originalAutoCommit) throws DLException {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            throw new DLException(e, "Operation=restoreAutoCommit", "DatabaseType=MySQL");
        }
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
