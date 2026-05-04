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
    private final String connectionParams;

    public MySQLDatabase(String host, String port, String database, String username, String password) {
        this(host, port, database, username, password, "");
    }

    public MySQLDatabase(String host, String port, String database, String username, String password, String connectionParams) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.connectionParams = connectionParams;
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

    public int setDataReturnKey(Connection connection, String sql, ArrayList<String> values) throws DLException {
        try (PreparedStatement statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            bindValues(statement, values, 1);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new DLException(e, "Operation=setDataReturnKeyConnection", "DatabaseType=MySQL", "SQL=" + sql);
        } catch (Exception e) {
            throw new DLException(e, "Operation=setDataReturnKeyConnection", "DatabaseType=MySQL", "SQL=" + sql);
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

    public boolean setData(Connection connection, String sql, ArrayList<String> values) throws DLException {
        try (PreparedStatement statement = prepare(connection, sql, values)) {
            statement.executeUpdate();
            return true;
        } catch (DLException e) {
            throw e;
        } catch (SQLException e) {
            throw new DLException(e, "Operation=setDataPreparedConnection", "DatabaseType=MySQL", "SQL=" + sql);
        } catch (Exception e) {
            throw new DLException(e, "Operation=setDataPreparedConnection", "DatabaseType=MySQL", "SQL=" + sql);
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
                String v = values.get(i);
                int idx = startIndex + i;
                if (v == null) {
                    statement.setNull(idx, java.sql.Types.VARCHAR);
                    continue;
                }
                // Bind integers and decimals as numeric types so LIMIT/OFFSET and price
                // comparisons work correctly — MySQL rejects LIMIT '50' (quoted string).
                try {
                    statement.setInt(idx, Integer.parseInt(v));
                    continue;
                } catch (NumberFormatException ignored) {}
                try {
                    statement.setDouble(idx, Double.parseDouble(v));
                    continue;
                } catch (NumberFormatException ignored) {}
                statement.setString(idx, v);
            }
        } catch (SQLException e) {
            throw new DLException(e, "Operation=bindValues", "DatabaseType=MySQL");
        } catch (Exception e) {
            throw new DLException(e, "Operation=bindValues", "DatabaseType=MySQL");
        }
    }

    private Connection createConnection() throws SQLException {
        StringBuilder url = new StringBuilder("jdbc:mysql://")
            .append(host)
            .append(":")
            .append(port)
            .append("/")
            .append(database)
            .append("?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
        if (connectionParams != null && !connectionParams.isBlank()) {
            url.append("&").append(connectionParams);
        }

        Properties props = new Properties();
        props.put("user", username);
        props.put("password", password);

        return DriverManager.getConnection(url.toString(), props);
    }
}
