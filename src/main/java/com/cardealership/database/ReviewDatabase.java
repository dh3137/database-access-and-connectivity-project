package com.cardealership.database;

import com.cardealership.DLException;
import com.cardealership.util.MySQLDatabase;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReviewDatabase {

    private final MySQLDatabase database;

    public ReviewDatabase(MySQLDatabase database) {
        this.database = database;
    }

    public List<Map<String, Object>> getReviewsByModelId(int modelId) throws DLException {
        String sql = "SELECT review_id, author_name, rating, review_text, source, created_at " +
                     "FROM Reviews WHERE model_id = ? ORDER BY created_at DESC";
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(modelId));
        String[][] rows = database.getData(sql, params);

        List<Map<String, Object>> reviews = new ArrayList<>();
        for (int i = 1; i < rows.length; i++) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("reviewId",   rows[i][0]);
            r.put("authorName", rows[i][1]);
            r.put("rating",     rows[i][2]);
            r.put("reviewText", rows[i][3]);
            r.put("source",     rows[i][4]);
            r.put("createdAt",  rows[i][5]);
            reviews.add(r);
        }
        return reviews;
    }

    public double getAverageRating(int modelId) throws DLException {
        String sql = "SELECT AVG(rating) FROM Reviews WHERE model_id = ?";
        ArrayList<String> params = new ArrayList<>();
        params.add(String.valueOf(modelId));
        String[][] rows = database.getData(sql, params);
        if (rows.length < 2 || rows[1][0] == null || rows[1][0].isBlank()) return 0.0;
        return Double.parseDouble(rows[1][0]);
    }
}
