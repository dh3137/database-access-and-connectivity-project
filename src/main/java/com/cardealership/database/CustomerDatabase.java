package com.cardealership.database;

import java.util.ArrayList;
import java.util.List;

import com.cardealership.DLException;
import com.cardealership.model.Customer;
import com.cardealership.util.MySQLDatabase;

/**
 * Data-access class for the Customers table.
 */
public class CustomerDatabase {

    private final MySQLDatabase database;

    public CustomerDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<Customer> getAllCustomers() throws DLException {
        String sql = "SELECT customer_id, firstName, lastName, email, phone FROM Customers ORDER BY lastName, firstName";
        String[][] rows = database.getData(sql, new ArrayList<>());
        List<Customer> list = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            list.add(mapCustomer(rows[i]));
        }
        return list;
    }

    public Customer getCustomerById(int id) throws DLException {
        String sql = "SELECT customer_id, firstName, lastName, email, phone FROM Customers WHERE customer_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapCustomer(rows[1]) : null;
    }

    public Customer getCustomerByEmail(String email) throws DLException {
        String sql = "SELECT customer_id, firstName, lastName, email, phone FROM Customers WHERE email = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(email);
        String[][] rows = database.getData(sql, values);
        return rows.length > 1 ? mapCustomer(rows[1]) : null;
    }

    public boolean saveCustomer(Customer customer) throws DLException {
        String sql = "INSERT INTO Customers (firstName, lastName, email, phone) VALUES (?, ?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(customer.getFirstName());
        values.add(customer.getLastName());
        values.add(customer.getEmail());
        values.add(customer.getPhone() != null ? customer.getPhone() : "");
        return database.setData(sql, values);
    }

    public boolean updateCustomer(Customer customer) throws DLException {
        String sql = "UPDATE Customers SET firstName = ?, lastName = ?, email = ?, phone = ? WHERE customer_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(customer.getFirstName());
        values.add(customer.getLastName());
        values.add(customer.getEmail());
        values.add(customer.getPhone() != null ? customer.getPhone() : "");
        values.add(String.valueOf(customer.getCustomerId()));
        return database.setData(sql, values);
    }

    public boolean deleteCustomer(int id) throws DLException {
        String sql = "DELETE FROM Customers WHERE customer_id = ?";
        ArrayList<String> values = new ArrayList<>();
        values.add(String.valueOf(id));
        return database.setData(sql, values);
    }

    private Customer mapCustomer(String[] row) {
        Customer c = new Customer();
        c.setCustomerId(Integer.parseInt(row[0]));
        c.setFirstName(row[1]);
        c.setLastName(row[2]);
        c.setEmail(row[3]);
        c.setPhone(row[4]);
        return c;
    }
}
