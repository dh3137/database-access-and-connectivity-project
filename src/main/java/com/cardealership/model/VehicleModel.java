package com.cardealership.model;

/**
 * Represents a row in the Models table.
 * Named VehicleModel to avoid a name clash with java.lang.reflect.Method / java util.
 */
public class VehicleModel {

    private int modelId;
    private int manufacturerId;
    private String modelName;

    public VehicleModel() {}

    public VehicleModel(int modelId, int manufacturerId, String modelName) {
        this.modelId = modelId;
        this.manufacturerId = manufacturerId;
        this.modelName = modelName;
    }

    public int getModelId()                        { return modelId; }
    public void setModelId(int modelId)            { this.modelId = modelId; }

    public int getManufacturerId()                 { return manufacturerId; }
    public void setManufacturerId(int id)          { this.manufacturerId = id; }

    public String getModelName()                   { return modelName; }
    public void setModelName(String modelName)     { this.modelName = modelName; }
}
