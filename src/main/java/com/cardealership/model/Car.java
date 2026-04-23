package com.cardealership.model;

public class Car {

    private int id;           // vehicle_id
    private int manufacturerId;
    private int modelId;
    private String make;      // Manufacturers.name  (read-only, populated by JOIN)
    private String model;     // Models.model_name   (read-only, populated by JOIN)
    private int year;
    private double price;
    private String status;    // Available | Reserved | Sold
    private String color;
    private int mileage;
    private String vin;
    private String imageUrl;  // primary image from VehicleImages (populated by JOIN)
    private String description;
    private String segment;

    public Car() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(int manufacturerId) { this.manufacturerId = manufacturerId; }

    public int getModelId() { return modelId; }
    public void setModelId(int modelId) { this.modelId = modelId; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }
}
