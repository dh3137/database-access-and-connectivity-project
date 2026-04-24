package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.Manufacturer;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the Manufacturers table.
 */
public class ManufacturerDatabase {

    private final MySQLDatabase database;

    public ManufacturerDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<Manufacturer> getAllManufacturers() throws DLException {
        String sql = "SELECT manufacturer_id, name FROM Manufacturers ORDER BY name";
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<Manufacturer> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapManufacturer(rows[i]));
        }
        return list;
    }

    public Manufacturer getManufacturerById(int id) throws DLException {
        String sql = "SELECT manufacturer_id, name FROM Manufacturers WHERE manufacturer_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapManufacturer(rows[1]) : null;
    }

    public boolean saveManufacturer(Manufacturer manufacturer) throws DLException {
        String sql = "INSERT INTO Manufacturers (name) VALUES (?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(manufacturer.getName());
        return database.setData(sql, values);
    }

    public boolean updateManufacturer(Manufacturer manufacturer) throws DLException {
        String sql = "UPDATE Manufacturers SET name = ? WHERE manufacturer_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(manufacturer.getName());
        values.add(String.valueOf(manufacturer.getManufacturerId()));
        return database.setData(sql, values);
    }

    public boolean deleteManufacturer(int id) throws DLException {
        String sql = "DELETE FROM Manufacturers WHERE manufacturer_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        return database.setData(sql, values);
    }

    private Manufacturer mapManufacturer(String[] row) {
        Manufacturer m = new Manufacturer();
        m.setManufacturerId(Integer.parseInt(row[0]));
        m.setName(row[1]);
        return m;
    }
}
