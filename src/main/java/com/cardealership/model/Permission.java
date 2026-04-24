package com.cardealership.model;

/**
 * Represents a row in the Permissions table.
 */
public class Permission {

    private int permissionId;
    private String permissionName;

    public Permission() {}

    public Permission(int permissionId, String permissionName) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
    }

    public int getPermissionId()                          { return permissionId; }
    public void setPermissionId(int permissionId)         { this.permissionId = permissionId; }

    public String getPermissionName()                     { return permissionName; }
    public void setPermissionName(String permissionName)  { this.permissionName = permissionName; }
}
