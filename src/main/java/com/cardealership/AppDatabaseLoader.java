package com.cardealership;

import com.cardealership.util.MySQLDatabase;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads the database connection settings for the current OS user from
 * {@code db.properties}. Each teammate keeps their own local credentials file.
 */
public final class AppDatabaseLoader {

    private AppDatabaseLoader() {}

    public static MySQLDatabase load() {
        String osUser = System.getProperty("user.name");
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(
                "Could not read db.properties file. Copy db.properties.example to db.properties first.",
                e
            );
        }

        String host = props.getProperty(osUser + ".host");
        String port = props.getProperty(osUser + ".port");
        String database = props.getProperty(osUser + ".database");
        String username = props.getProperty(osUser + ".username");
        String password = props.getProperty(osUser + ".password");
        String params = props.getProperty(osUser + ".params", "");

        if (host == null || port == null || database == null || username == null || password == null) {
            throw new RuntimeException(
                "No DB config found for OS user: \"" + osUser + "\". Add your entry to db.properties."
            );
        }

        return new MySQLDatabase(host, port, database, username, password, params);
    }
}
