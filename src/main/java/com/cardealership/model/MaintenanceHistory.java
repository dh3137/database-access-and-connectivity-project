package com.cardealership.model;

/**
 * Represents a row in the MaintenanceHistory table.
 */
public class MaintenanceHistory {

    private int maintenanceId;
    private int vehicleId;
    private String serviceDate; // yyyy-MM-dd
    private String description;

    public MaintenanceHistory() {}

    public MaintenanceHistory(int maintenanceId, int vehicleId, String serviceDate, String description) {
        this.maintenanceId = maintenanceId;
        this.vehicleId = vehicleId;
        this.serviceDate = serviceDate;
        this.description = description;
    }

    public int getMaintenanceId()                       { return maintenanceId; }
    public void setMaintenanceId(int maintenanceId)     { this.maintenanceId = maintenanceId; }

    public int getVehicleId()                           { return vehicleId; }
    public void setVehicleId(int vehicleId)             { this.vehicleId = vehicleId; }

    public String getServiceDate()                      { return serviceDate; }
    public void setServiceDate(String serviceDate)      { this.serviceDate = serviceDate; }

    public String getDescription()                      { return description; }
    public void setDescription(String description)      { this.description = description; }
}
