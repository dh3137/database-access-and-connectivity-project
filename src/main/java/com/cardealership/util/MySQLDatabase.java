package com.cardealership.util;

import com.cardealership.DLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class MySQLDatabase {

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;

    public MySQLDatabase(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public boolean connect() throws DLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection connection = createConnection()) {
                System.out.println("Connected to database successfully.");
                return true;
            }
        } catch (ClassNotFoundException e) {
            throw new DLException(e, "Operation=connect", "DatabaseType=MySQL", "Cause=DriverNotFound");
        } catch (SQLException e) {
            throw new DLException(e, "Operation=connect", "DatabaseType=MySQL");
        } catch (Exception e) {
            throw new DLException(e, "Operation=connect", "DatabaseType=MySQL");
        }
    }

    public Connection getConnection() throws DLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return createConnection();
        } catch (ClassNotFoundException e) {
            throw new DLException(e, "Operation=getConnection", "DatabaseType=MySQL", "Cause=DriverNotFound");
        } catch (SQLException e) {
            throw new DLException(e, "Operation=getConnection", "DatabaseType=MySQL");
        } catch (Exception e) {
            throw new DLException(e, "Operation=getConnection", "DatabaseType=MySQL");
        }
    }

    public int setDataReturnKey(String sql, ArrayList<String> values) throws DLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            bindValues(statement, values, 1);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (DLException e) {
            throw e;
        } catch (SQLException e) {
            throw new DLException(e, "Operation=setDataReturnKey", "DatabaseType=MySQL", "SQL=" + sql);
        } catch (Exception e) {
            throw new DLException(e, "Operation=setDataReturnKey", "DatabaseType=MySQL", "SQL=" + sql);
        }
    }

    public boolean setData(String sql, ArrayList<String> values) throws DLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = prepare(connection, sql, values)) {

            statement.executeUpdate();
            return true;
        } catch (DLException e) {
            throw e;
        } catch (SQLException e) {
            throw new DLException(e, "Operation=setDataPrepared", "DatabaseType=MySQL", "SQL=" + sql);
        } catch (Exception e) {
            throw new DLException(e, "Operation=setDataPrepared", "DatabaseType=MySQL", "SQL=" + sql);
        }
    }

    public String[][] getData(String sql, ArrayList<String> values) throws DLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = prepare(connection, sql, values);
             ResultSet resultSet = statement.executeQuery()) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            ArrayList<String[]> rows = new ArrayList<>();

            String[] header = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                header[i - 1] = metaData.getColumnName(i);
            }
            rows.add(header);

            while (resultSet.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getString(i);
                }
                rows.add(row);
            }

            return rows.toArray(new String[0][]);
        } catch (DLException e) {
            throw e;
        } catch (SQLException e) {
            throw new DLException(e, "Operation=getDataPrepared", "DatabaseType=MySQL", "SQL=" + sql);
        } catch (Exception e) {
            throw new DLException(e, "Operation=getDataPrepared", "DatabaseType=MySQL", "SQL=" + sql);
        }
    }

    public PreparedStatement prepare(Connection connection, String sql, ArrayList<String> values) throws DLException {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            bindValues(statement, values, 1);
            return statement;
        } catch (SQLException e) {
            throw new DLException(e, "Operation=prepare", "DatabaseType=MySQL", "SQL=" + sql);
        } catch (Exception e) {
            throw new DLException(e, "Operation=prepare", "DatabaseType=MySQL", "SQL=" + sql);
        }
    }

    private void bindValues(PreparedStatement statement, ArrayList<String> values, int startIndex) throws DLException {
        try {
            for (int i = 0; i < values.size(); i++) {
                statement.setString(startIndex + i, values.get(i));
            }
        } catch (SQLException e) {
            throw new DLException(e, "Operation=bindValues", "DatabaseType=MySQL");
        } catch (Exception e) {
            throw new DLException(e, "Operation=bindValues", "DatabaseType=MySQL");
        }
    }

    private Connection createConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";

        Properties props = new Properties();
        props.put("user", username);
        props.put("password", password);

        return DriverManager.getConnection(url, props);
    }
}
