package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.VehicleModel;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the Models table.
 */
public class VehicleModelDatabase {

    private final MySQLDatabase database;

    public VehicleModelDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<VehicleModel> getAllModels() throws DLException {
        String sql = "SELECT model_id, manufacturer_id, model_name FROM Models ORDER BY model_name";
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<VehicleModel> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapModel(rows[i]));
        }
        return list;
    }

    public List<VehicleModel> getModelsByManufacturer(int manufacturerId) throws DLException {
        String sql = "SELECT model_id, manufacturer_id, model_name FROM Models WHERE manufacturer_id = ? ORDER BY model_name";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(manufacturerId));
        String[][] rows = database.getData(sql, values);
        List<VehicleModel> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapModel(rows[i]));
        }
        return list;
    }

    public VehicleModel getModelById(int id) throws DLException {
        String sql = "SELECT model_id, manufacturer_id, model_name FROM Models WHERE model_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapModel(rows[1]) : null;
    }

    public boolean saveModel(VehicleModel model) throws DLException {
        String sql = "INSERT INTO Models (manufacturer_id, model_name) VALUES (?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(model.getManufacturerId()));
        values.add(model.getModelName());
        return database.setData(sql, values);
    }

    public boolean updateModel(VehicleModel model) throws DLException {
        String sql = "UPDATE Models SET manufacturer_id = ?, model_name = ? WHERE model_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(model.getManufacturerId()));
        values.add(model.getModelName());
        values.add(String.valueOf(model.getModelId()));
        return database.setData(sql, values);
    }

    public boolean deleteModel(int id) throws DLException {
        String sql = "DELETE FROM Models WHERE model_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        return database.setData(sql, values);
    }

    private VehicleModel mapModel(String[] row) {
        VehicleModel m = new VehicleModel();
        m.setModelId(Integer.parseInt(row[0]));
        m.setManufacturerId(Integer.parseInt(row[1]));
        m.setModelName(row[2]);
        return m;
    }
}
