package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.Sale;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the Sales table.
 */
public class SaleDatabase {

    private final MySQLDatabase database;

    public SaleDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<Sale> getAllSales() throws DLException {
        String sql = "SELECT sale_id, sale_date, vehicle_id, customer_id, emp_id FROM Sales ORDER BY sale_date DESC";
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<Sale> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapSale(rows[i]));
        }
        return list;
    }

    public Sale getSaleById(int saleId) throws DLException {
        String sql = "SELECT sale_id, sale_date, vehicle_id, customer_id, emp_id FROM Sales WHERE sale_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(saleId));
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapSale(rows[1]) : null;
    }

    public boolean saveSale(Sale sale) throws DLException {
        String sql = "INSERT INTO Sales (sale_date, vehicle_id, customer_id, emp_id) VALUES (?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(sale.getSaleDate());
        values.add(String.valueOf(sale.getVehicleId()));
        values.add(String.valueOf(sale.getCustomerId()));
        values.add(String.valueOf(sale.getEmpId()));
        return database.setData(sql, values);
    }

    public boolean deleteSale(int saleId) throws DLException {
        String sql = "DELETE FROM Sales WHERE sale_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(saleId));
        return database.setData(sql, values);
    }

    private Sale mapSale(String[] row) {
        Sale s = new Sale();
        s.setSaleId(Integer.parseInt(row[0]));
        s.setSaleDate(row[1]);
        s.setVehicleId(Integer.parseInt(row[2]));
        s.setCustomerId(Integer.parseInt(row[3]));
        s.setEmpId(Integer.parseInt(row[4]));
        return s;
    }
}
