package com.cardealership.model;

/**
 * Represents a row in the Vehicles table.
 * vehicle_id, vin, model_id, price, status
 *
 * For display purposes the DAO joins Manufacturers and Models so that
 * manufacturerName and modelName are populated as well.
 */
public class Vehicle {

    private int vehicleId;
    private String vin;
    private int modelId;
    private int year;
    private double price;
    private String status; // Available | Reserved | Sold
    private String color;
    private int mileage;
    private String description;
    private String imageUrl; // Joined from VehicleImages

    // Joined from Models / Manufacturers for display
    private String modelName;
    private String manufacturerName;

    public Vehicle() {}

    public Vehicle(int vehicleId, String vin, int modelId, int year, double price, String status) {
        this.vehicleId = vehicleId;
        this.vin = vin;
        this.modelId = modelId;
        this.year = year;
        this.price = price;
        this.status = status;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public int getVehicleId()                     { return vehicleId; }
    public void setVehicleId(int vehicleId)        { this.vehicleId = vehicleId; }

    public String getVin()                         { return vin; }
    public void setVin(String vin)                 { this.vin = vin; }

    public int getModelId()                        { return modelId; }
    public void setModelId(int modelId)            { this.modelId = modelId; }

    public int getYear()                           { return year; }
    public void setYear(int year)                  { this.year = year; }

    public double getPrice()                       { return price; }
    public void setPrice(double price)             { this.price = price; }

    public String getStatus()                      { return status; }
    public void setStatus(String status)           { this.status = status; }

    public String getColor()                       { return color; }
    public void setColor(String color)             { this.color = color; }

    public int getMileage()                        { return mileage; }
    public void setMileage(int mileage)            { this.mileage = mileage; }

    public String getDescription()                 { return description; }
    public void setDescription(String d)           { this.description = d; }

    public String getImageUrl()                    { return imageUrl; }
    public void setImageUrl(String url)            { this.imageUrl = url; }

    public String getModelName()                   { return modelName; }
    public void setModelName(String modelName)     { this.modelName = modelName; }

    public String getManufacturerName()            { return manufacturerName; }
    public void setManufacturerName(String n)      { this.manufacturerName = n; }
}
