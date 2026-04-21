package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;
import java.util.ArrayList;
import java.util.List;

public class ActionLogDatabase {

    private final MySQLDatabase database;

    public ActionLogDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public boolean saveActionLog(String username, String action, String detail) throws DLException {
        String sql = "INSERT INTO action_log (username, action, detail) VALUES (?, ?, ?)";
        ArrayList<String> values = new ArrayList<>();
        values.add(username);
        values.add(action);
        values.add(detail != null ? detail : "");
        return database.setData(sql, values);
    }

    public List<String[]> getRecent(int limit) throws DLException {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT username, action, detail, created_at FROM action_log ORDER BY created_at DESC LIMIT " + limit;
        ArrayList<String> values = new ArrayList<>();
        String[][] result = database.getData(sql, values);

        for (int i = 1; i < result.length; i++) {
            rows.add(result[i]);
        }

        return rows;
    }
}
