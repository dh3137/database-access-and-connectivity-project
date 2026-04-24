package com.cardealership.model;

/**
 * Represents a row in the Sales table.
 */
public class Sale {

    private int saleId;
    private String saleDate; // stored as DATE in DB, represented as String (yyyy-MM-dd)
    private int vehicleId;
    private int customerId;
    private int empId;

    public Sale() {}

    public Sale(int saleId, String saleDate, int vehicleId, int customerId, int empId) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.empId = empId;
    }

    public int getSaleId()                      { return saleId; }
    public void setSaleId(int saleId)           { this.saleId = saleId; }

    public String getSaleDate()                 { return saleDate; }
    public void setSaleDate(String saleDate)    { this.saleDate = saleDate; }

    public int getVehicleId()                   { return vehicleId; }
    public void setVehicleId(int vehicleId)     { this.vehicleId = vehicleId; }

    public int getCustomerId()                  { return customerId; }
    public void setCustomerId(int customerId)   { this.customerId = customerId; }

    public int getEmpId()                       { return empId; }
    public void setEmpId(int empId)             { this.empId = empId; }
}
