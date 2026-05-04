package com.cardealership;

import com.cardealership.database.ActionLogDatabase;
import com.cardealership.database.CarDatabase;
import com.cardealership.database.CustomerDatabase;
import com.cardealership.database.EmployeeDatabase;
import com.cardealership.database.EnquiryDatabase;
import com.cardealership.database.MaintenanceDatabase;
import com.cardealership.database.ReviewDatabase;
import com.cardealership.database.SalesDatabase;
import com.cardealership.database.UserDatabase;
import com.cardealership.util.MySQLDatabase;

/**
 * Small dependency container for the application's database-facing services.
 * Keeping them together makes controller construction much cleaner.
 */
public class AppContext {

    public final MySQLDatabase database;
    public final CarDatabase carDatabase;
    public final UserDatabase userDatabase;
    public final ActionLogDatabase actionLogDatabase;
    public final ReviewDatabase reviewDatabase;
    public final EnquiryDatabase enquiryDatabase;
    public final CustomerDatabase customerDatabase;
    public final EmployeeDatabase employeeDatabase;
    public final SalesDatabase salesDatabase;
    public final MaintenanceDatabase maintenanceDatabase;

    public AppContext(MySQLDatabase database) {
        this.database = database;
        this.carDatabase = new CarDatabase(database);
        this.userDatabase = new UserDatabase(database);
        this.actionLogDatabase = new ActionLogDatabase(database);
        this.reviewDatabase = new ReviewDatabase(database);
        this.enquiryDatabase = new EnquiryDatabase(database);
        this.customerDatabase = new CustomerDatabase(database);
        this.employeeDatabase = new EmployeeDatabase(database);
        this.salesDatabase = new SalesDatabase(database);
        this.maintenanceDatabase = new MaintenanceDatabase(database);
    }
}
