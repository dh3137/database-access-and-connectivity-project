package com.cardealership.model;

/**
 * Represents a row in the VehicleChangeLog table.
 */
public class VehicleChangeLog {

    private int logId;
    private int vehicleId;
    private int empId;
    private String changeType; // e.g. INSERT, UPDATE, DELETE
    private String changeDate; // DATETIME stored as String

    public VehicleChangeLog() {}

    public VehicleChangeLog(int logId, int vehicleId, int empId, String changeType, String changeDate) {
        this.logId = logId;
        this.vehicleId = vehicleId;
        this.empId = empId;
        this.changeType = changeType;
        this.changeDate = changeDate;
    }

    public int getLogId()                            { return logId; }
    public void setLogId(int logId)                  { this.logId = logId; }

    public int getVehicleId()                        { return vehicleId; }
    public void setVehicleId(int vehicleId)          { this.vehicleId = vehicleId; }

    public int getEmpId()                            { return empId; }
    public void setEmpId(int empId)                  { this.empId = empId; }

    public String getChangeType()                    { return changeType; }
    public void setChangeType(String changeType)     { this.changeType = changeType; }

    public String getChangeDate()                    { return changeDate; }
    public void setChangeDate(String changeDate)     { this.changeDate = changeDate; }
}
