package com.cardealership.controller;

import com.cardealership.AppContext;
import com.cardealership.DLException;
import com.cardealership.model.Car;
import com.cardealership.model.User;
import com.cardealership.service.AuthService;
import com.cardealership.util.HttpUtil;
import com.cardealership.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * Owns catalogue-style endpoints: cars, reviews, logs, images, and maintenance.
 */
public class CatalogController {

    private final AppContext context;
    private final AuthService authService;

    public CatalogController(AppContext context, AuthService authService) {
        this.context = context;
        this.authService = authService;
    }

    public void handleCars(HttpExchange ex) throws IOException {
        try {
            User user = authService.getSessionUser(ex);
            String method = ex.getRequestMethod();
            String[] parts = ex.getRequestURI().getPath().split("/");
            boolean hasId = parts.length >= 4 && !parts[3].isEmpty();

            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method)) {
                if (user == null) { HttpUtil.sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
                if (!authService.canManageCars(user)) {
                    HttpUtil.sendJson(ex, 403, "{\"error\":\"Forbidden\"}");
                    return;
                }
            }

            if ("DELETE".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                boolean deleted = context.carDatabase.deleteCar(id);
                if (deleted) logVehicleChange(id, user.getEmpId(), "DELETE", null, null);
                HttpUtil.sendJson(ex, deleted ? 200 : 404, deleted ? "{\"ok\":true}" : "{\"error\":\"Car not found\"}");
            } else if ("PATCH".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                String status = JsonUtil.jsonString(HttpUtil.readBody(ex), "status");
                if (status.isBlank()) { HttpUtil.sendJson(ex, 400, "{\"error\":\"status is required\"}"); return; }
                String dbStatus = toDatabaseStatus(status);
                boolean updated = context.carDatabase.updateCarStatus(id, dbStatus);
                if (updated) logVehicleChange(id, user.getEmpId(), "UPDATE", "status", dbStatus);
                HttpUtil.sendJson(ex, updated ? 200 : 404, updated
                    ? "{\"ok\":true,\"status\":\"" + toApiStatus(dbStatus) + "\"}"
                    : "{\"error\":\"Car not found\"}");
            } else if ("PUT".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                Car car = parseCarJson(HttpUtil.readBody(ex));
                car.setId(id);
                if (car.getModelId() <= 0 && hasModelText(car)) {
                    car.setModelId(context.carDatabase.findOrCreateModelId(
                        car.getMake(), car.getModel(), car.getBodyType(), car.getSegment(), car.getCountry()
                    ));
                }
                validateCar(car, true);
                boolean updated = context.carDatabase.updateCar(car);
                if (updated) {
                    logVehicleChange(id, user.getEmpId(), "UPDATE", "vehicle", String.valueOf(id));
                    if (car.getImageUrl() != null && !car.getImageUrl().isBlank()) {
                        context.carDatabase.upsertVehicleImage(id, car.getImageUrl());
                    }
                }
                HttpUtil.sendJson(ex, updated ? 200 : 404, updated ? carToJson(car) : "{\"error\":\"Car not found\"}");
            } else if ("POST".equals(method) && !hasId) {
                Car car = parseCarJson(HttpUtil.readBody(ex));
                if (car.getModelId() <= 0 && hasModelText(car)) {
                    car.setModelId(context.carDatabase.findOrCreateModelId(
                        car.getMake(), car.getModel(), car.getBodyType(), car.getSegment(), car.getCountry()
                    ));
                }
                if (car.getVin() == null || car.getVin().isBlank()) {
                    car.setVin(context.carDatabase.generateUniqueVin());
                }
                validateCar(car, false);
                boolean added = context.carDatabase.saveCar(car);
                if (added) logVehicleChange(car.getModelId(), user.getEmpId(), "INSERT", null, null);
                HttpUtil.sendJson(ex, added ? 201 : 500, added ? "{\"ok\":true}" : "{\"error\":\"Could not add car\"}");
            } else if ("GET".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                Car car = context.carDatabase.getCarById(id);
                HttpUtil.sendJson(ex, car == null ? 404 : 200, car == null ? "{\"error\":\"Car not found\"}" : carToJson(car));
            } else if ("GET".equals(method)) {
                List<Car> cars = context.carDatabase.getAllCars();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < cars.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(carToJson(cars.get(i)));
                }
                sb.append("]");
                HttpUtil.sendJson(ex, 200, sb.toString());
            } else {
                HttpUtil.sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof NumberFormatException) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"" + JsonUtil.escapeJson(e.getMessage() != null ? e.getMessage() : "Invalid car data") + "\"}");
                return;
            }
            System.err.println("[cars] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleLogs(HttpExchange ex) throws IOException {
        try {
            User user = authService.getSessionUser(ex);
            if (user == null) { HttpUtil.sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!authService.canAccessStaffFeatures(user)) { HttpUtil.sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

            List<String[]> entries = context.actionLogDatabase.getRecent(50);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) sb.append(",");
                String[] entry = entries.get(i);
                sb.append(String.format(
                    "{\"vehicleId\":\"%s\",\"employee\":\"%s %s\",\"changeType\":\"%s\",\"fieldChanged\":\"%s\",\"newValue\":\"%s\",\"time\":\"%s\"}",
                    JsonUtil.escapeJson(entry[0] != null ? entry[0] : ""),
                    JsonUtil.escapeJson(entry[1] != null ? entry[1] : ""),
                    JsonUtil.escapeJson(entry[2] != null ? entry[2] : ""),
                    JsonUtil.escapeJson(entry[3] != null ? entry[3] : ""),
                    JsonUtil.escapeJson(entry[4] != null ? entry[4] : ""),
                    JsonUtil.escapeJson(entry[5] != null ? entry[5] : ""),
                    JsonUtil.escapeJson(entry[6] != null ? entry[6] : "")
                ));
            }
            sb.append("]");
            HttpUtil.sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[logs] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleCarImage(HttpExchange ex) throws IOException {
        try {
            Map<String, String> params = HttpUtil.parseQuery(ex.getRequestURI().getQuery());
            String make = params.getOrDefault("make", "").trim();
            String model = params.getOrDefault("model", "").trim();
            String year = params.getOrDefault("year", "").trim();
            String vehicleIdStr = params.getOrDefault("vehicleId", "").trim();

            if (make.isEmpty() || model.isEmpty()) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"make and model are required\"}");
                return;
            }

            String imageUrl = null;
            if (!vehicleIdStr.isEmpty()) {
                try {
                    imageUrl = context.carDatabase.getVehicleImageUrl(Integer.parseInt(vehicleIdStr));
                } catch (Exception ignored) {}
            }
            if (imageUrl == null) {
                imageUrl = fetchWikipediaImage(make, model, year);
            }

            if (imageUrl != null) {
                HttpUtil.sendJson(ex, 200, "{\"url\":\"" + JsonUtil.escapeJson(imageUrl) + "\"}");
            } else {
                HttpUtil.sendJson(ex, 404, "{\"error\":\"No image found\"}");
            }
        } catch (Exception e) {
            System.err.println("[carimage] ERROR: " + e.getMessage());
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleModels(HttpExchange ex) throws IOException {
        try {
            String[] parts = ex.getRequestURI().getPath().split("/");
            if (parts.length < 5 || !"reviews".equals(parts[4])) {
                HttpUtil.sendJson(ex, 404, "{\"error\":\"Not found\"}");
                return;
            }
            int modelId = Integer.parseInt(parts[3]);
            double avg = context.reviewDatabase.getAverageRating(modelId);
            List<Map<String, Object>> reviews = context.reviewDatabase.getReviewsByModelId(modelId);

            StringBuilder sb = new StringBuilder();
            sb.append("{\"averageRating\":").append(String.format("%.2f", avg)).append(",\"reviews\":[");
            for (int i = 0; i < reviews.size(); i++) {
                if (i > 0) sb.append(",");
                Map<String, Object> review = reviews.get(i);
                sb.append("{")
                    .append("\"reviewId\":").append(review.get("reviewId")).append(",")
                    .append("\"authorName\":\"").append(JsonUtil.escapeJson((String) review.get("authorName"))).append("\",")
                    .append("\"rating\":").append(review.get("rating")).append(",")
                    .append("\"reviewText\":\"").append(JsonUtil.escapeJson((String) review.get("reviewText"))).append("\",")
                    .append("\"source\":\"").append(JsonUtil.escapeJson((String) review.get("source"))).append("\",")
                    .append("\"createdAt\":\"").append(JsonUtil.escapeJson((String) review.get("createdAt"))).append("\"")
                    .append("}");
            }
            sb.append("]}");
            HttpUtil.sendJson(ex, 200, sb.toString());
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(ex, 400, "{\"error\":\"Invalid model id\"}");
        } catch (Exception e) {
            System.err.println("[models] ERROR: " + e.getMessage());
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleReviews(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equals(ex.getRequestMethod())) {
                HttpUtil.sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            List<Map<String, Object>> reviews = context.reviewDatabase.getAllReviews();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < reviews.size(); i++) {
                if (i > 0) sb.append(",");
                Map<String, Object> review = reviews.get(i);
                sb.append("{")
                    .append("\"reviewId\":").append(review.get("reviewId")).append(",")
                    .append("\"authorName\":\"").append(JsonUtil.escapeJson((String) review.get("authorName"))).append("\",")
                    .append("\"rating\":").append(review.get("rating")).append(",")
                    .append("\"reviewText\":\"").append(JsonUtil.escapeJson((String) review.get("reviewText"))).append("\",")
                    .append("\"source\":\"").append(JsonUtil.escapeJson((String) review.get("source"))).append("\",")
                    .append("\"createdAt\":\"").append(JsonUtil.escapeJson((String) review.get("createdAt"))).append("\",")
                    .append("\"modelName\":\"").append(JsonUtil.escapeJson((String) review.get("modelName"))).append("\",")
                    .append("\"manufacturerName\":\"").append(JsonUtil.escapeJson((String) review.get("manufacturerName"))).append("\"")
                    .append("}");
            }
            sb.append("]");
            HttpUtil.sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[reviews] ERROR: " + e.getMessage());
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleMaintenance(HttpExchange ex) throws IOException {
        try {
            String[] parts = ex.getRequestURI().getPath().split("/");
            if (parts.length < 4 || parts[3].isEmpty()) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"vehicleId is required\"}");
                return;
            }
            int vehicleId = Integer.parseInt(parts[3]);

            if ("GET".equals(ex.getRequestMethod())) {
                String[][] rows = context.maintenanceDatabase.getByVehicleId(vehicleId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 1; i < rows.length; i++) {
                    if (i > 1) sb.append(",");
                    String[] row = rows[i];
                    sb.append(String.format(
                        "{\"id\":\"%s\",\"serviceDate\":\"%s\",\"serviceType\":\"%s\",\"description\":\"%s\",\"cost\":\"%s\",\"performedBy\":\"%s\"}",
                        JsonUtil.escapeJson(row[0] != null ? row[0] : ""),
                        JsonUtil.escapeJson(row[1] != null ? row[1] : ""),
                        JsonUtil.escapeJson(row[2] != null ? row[2] : ""),
                        JsonUtil.escapeJson(row[3] != null ? row[3] : ""),
                        JsonUtil.escapeJson(row[4] != null ? row[4] : ""),
                        JsonUtil.escapeJson(row[5] != null ? row[5] : "")
                    ));
                }
                sb.append("]");
                HttpUtil.sendJson(ex, 200, sb.toString());
            } else if ("POST".equals(ex.getRequestMethod())) {
                User user = authService.getSessionUser(ex);
                if (user == null) { HttpUtil.sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
                if (!authService.canAccessStaffFeatures(user)) { HttpUtil.sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

                String body = HttpUtil.readBody(ex);
                String serviceDate = JsonUtil.jsonString(body, "serviceDate").trim();
                String serviceType = JsonUtil.jsonString(body, "serviceType").trim();
                String description = JsonUtil.jsonString(body, "description").trim();
                String cost = JsonUtil.jsonString(body, "cost").trim();
                String performedBy = JsonUtil.jsonString(body, "performedBy").trim();

                if (serviceDate.isEmpty() || serviceType.isEmpty()) {
                    HttpUtil.sendJson(ex, 400, "{\"error\":\"serviceDate and serviceType are required\"}");
                    return;
                }
                if (!cost.isEmpty()) {
                    try {
                        if (Double.parseDouble(cost) < 0) {
                            HttpUtil.sendJson(ex, 400, "{\"error\":\"cost cannot be negative\"}");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        HttpUtil.sendJson(ex, 400, "{\"error\":\"cost must be a valid number\"}");
                        return;
                    }
                }

                boolean ok = context.maintenanceDatabase.addRecord(vehicleId, serviceDate, serviceType, description, cost, performedBy);
                if (ok) {
                    authService.logGeneralAction(
                        user,
                        "MAINTENANCE_ADDED",
                        "Vehicle",
                        String.valueOf(vehicleId),
                        "Added maintenance record: " + serviceType
                    );
                }
                HttpUtil.sendJson(ex, ok ? 201 : 500, ok ? "{\"ok\":true}" : "{\"error\":\"Could not save record\"}");
            } else {
                HttpUtil.sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(ex, 400, "{\"error\":\"Invalid vehicleId\"}");
        } catch (Exception e) {
            System.err.println("[maintenance] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    private boolean hasModelText(Car car) {
        return car.getMake() != null && !car.getMake().isBlank()
            && car.getModel() != null && !car.getModel().isBlank();
    }

    private String carToJson(Car car) {
        return String.format(
            "{\"id\":%d,\"make\":\"%s\",\"model\":\"%s\",\"year\":%d,\"price\":%.2f,\"status\":\"%s\",\"color\":\"%s\",\"mileage\":%d,\"imageUrl\":\"%s\",\"description\":\"%s\",\"vin\":\"%s\",\"modelId\":%d,\"manufacturerId\":%d,\"segment\":\"%s\"}",
            car.getId(),
            JsonUtil.escapeJson(car.getMake()),
            JsonUtil.escapeJson(car.getModel()),
            car.getYear(),
            car.getPrice(),
            JsonUtil.escapeJson(toApiStatus(car.getStatus())),
            JsonUtil.escapeJson(car.getColor() != null ? car.getColor() : ""),
            car.getMileage(),
            JsonUtil.escapeJson(car.getImageUrl() != null ? car.getImageUrl() : ""),
            JsonUtil.escapeJson(car.getDescription() != null ? car.getDescription() : ""),
            JsonUtil.escapeJson(car.getVin() != null ? car.getVin() : ""),
            car.getModelId(),
            car.getManufacturerId(),
            JsonUtil.escapeJson(car.getSegment() != null ? car.getSegment() : "")
        );
    }

    private Car parseCarJson(String json) {
        Car car = new Car();
        car.setMake(JsonUtil.jsonString(json, "make"));
        car.setModel(JsonUtil.jsonString(json, "model"));
        String modelIdStr = JsonUtil.jsonString(json, "modelId");
        car.setModelId(modelIdStr.isEmpty() ? 0 : Integer.parseInt(modelIdStr));
        String yearStr = JsonUtil.jsonString(json, "year");
        car.setYear(yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr));
        String priceStr = JsonUtil.jsonString(json, "price");
        car.setPrice(priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr));
        car.setStatus(toDatabaseStatus(JsonUtil.jsonString(json, "status")));
        car.setColor(JsonUtil.jsonString(json, "color"));
        String mileageStr = JsonUtil.jsonString(json, "mileage");
        car.setMileage(mileageStr.isEmpty() ? 0 : Integer.parseInt(mileageStr));
        car.setVin(JsonUtil.jsonString(json, "vin"));
        car.setImageUrl(JsonUtil.jsonString(json, "imageUrl"));
        car.setDescription(JsonUtil.jsonString(json, "description"));
        car.setBodyType(JsonUtil.jsonString(json, "bodyType"));
        car.setSegment(JsonUtil.jsonString(json, "segment"));
        car.setCountry(JsonUtil.jsonString(json, "country"));
        return car;
    }

    private void validateCar(Car car, boolean update) {
        int maxYear = Year.now().getValue() + 1;
        if (car == null) {
            throw new IllegalArgumentException("Car data is required.");
        }
        if (update && car.getId() <= 0) {
            throw new IllegalArgumentException("Car id is required for update.");
        }
        if (car.getModelId() <= 0) {
            throw new IllegalArgumentException("modelId is required.");
        }
        if (car.getYear() < 1900 || car.getYear() > maxYear) {
            throw new IllegalArgumentException("Car year is not valid.");
        }
        if (car.getPrice() < 0) {
            throw new IllegalArgumentException("Car price cannot be negative.");
        }
        if (car.getMileage() < 0) {
            throw new IllegalArgumentException("Car mileage cannot be negative.");
        }
        if (car.getVin() != null && !car.getVin().isBlank() && car.getVin().trim().length() != 17) {
            throw new IllegalArgumentException("VIN must be exactly 17 characters.");
        }
    }

    private void logVehicleChange(int vehicleId, int empId, String changeType, String fieldChanged, String newValue) {
        try {
            context.actionLogDatabase.saveActionLog(vehicleId, empId, changeType, fieldChanged, newValue);
        } catch (DLException e) {
            System.err.println("[change_log] Failed to write log: " + e.getMessage());
        }
    }

    private String toApiStatus(String status) {
        if (status == null || status.isBlank()) return "AVAILABLE";
        return switch (status.trim().toLowerCase()) {
            case "sold" -> "SOLD";
            case "reserved" -> "RESERVED";
            default -> "AVAILABLE";
        };
    }

    private String toDatabaseStatus(String status) {
        if (status == null || status.isBlank()) return "Available";
        return switch (status.trim().toUpperCase()) {
            case "SOLD" -> "Sold";
            case "RESERVED" -> "Reserved";
            default -> "Available";
        };
    }

    private String fetchWikipediaImage(String make, String model, String year) {
        String[] queries = year.isEmpty()
            ? new String[] {make + " " + model}
            : new String[] {year + " " + make + " " + model, make + " " + model};

        for (String query : queries) {
            try {
                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String apiUrl = "https://en.wikipedia.org/w/api.php"
                    + "?action=query"
                    + "&generator=search"
                    + "&gsrsearch=" + encoded
                    + "&gsrlimit=5"
                    + "&prop=pageimages"
                    + "&pithumbsize=1200"
                    + "&pilimit=5"
                    + "&format=json";

                HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
                conn.setRequestProperty("User-Agent", "AutoPrime/1.0 (car-dealership-project)");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                String resp = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();

                String firstAny = null;
                int searchFrom = 0;
                while (true) {
                    int srcIdx = resp.indexOf("\"source\":\"", searchFrom);
                    if (srcIdx < 0) break;
                    int start = srcIdx + 10;
                    int end = resp.indexOf('"', start);
                    if (end <= start) break;
                    String candidate = resp.substring(start, end).replace("\\/", "/");
                    searchFrom = end + 1;
                    String lower = candidate.toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.contains(".jpg/")) {
                        return candidate;
                    }
                    if (firstAny == null) firstAny = candidate;
                }
                if (firstAny != null) return firstAny;
            } catch (Exception ignored) {}
        }
        return null;
    }
}
