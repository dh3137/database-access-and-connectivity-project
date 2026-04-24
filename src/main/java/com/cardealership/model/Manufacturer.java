package com.cardealership.model;

/**
 * Represents a row in the Manufacturers table.
 */
public class Manufacturer {

    private int manufacturerId;
    private String name;

    public Manufacturer() {}

    public Manufacturer(int manufacturerId, String name) {
        this.manufacturerId = manufacturerId;
        this.name = name;
    }

    public int getManufacturerId()                   { return manufacturerId; }
    public void setManufacturerId(int id)            { this.manufacturerId = id; }

    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }
}
