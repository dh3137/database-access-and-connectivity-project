package com.cardealership;

import com.cardealership.database.ActionLogDatabase;
import com.cardealership.database.CarDatabase;
import com.cardealership.database.UserDatabase;
import com.cardealership.model.Car;
import com.cardealership.model.User;
import com.cardealership.util.MySQLDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Main {

    private static final MySQLDatabase database = loadDatabase();

    private static MySQLDatabase loadDatabase() {
        String osUser = System.getProperty("user.name");
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Could not read db.properties file. Make sure it exists in the project root.", e);
        }
        String host     = props.getProperty(osUser + ".host");
        String port     = props.getProperty(osUser + ".port");
        String database = props.getProperty(osUser + ".database");
        String username = props.getProperty(osUser + ".username");
        String password = props.getProperty(osUser + ".password");
        if (host == null || password == null) {
            throw new RuntimeException("No DB config found for OS user: \"" + osUser + "\". Add your entry to db.properties.");
        }
        return new MySQLDatabase(host, port, database, username, password);
    }
    private static final Map<String, User> sessions = new ConcurrentHashMap<>();
    private static final CarDatabase carDatabase = new CarDatabase(database);
    private static final UserDatabase userDatabase = new UserDatabase(database);
    private static final ActionLogDatabase actionLogDatabase = new ActionLogDatabase(database);

    // Serve static files from src/main/webapp (works when run from project root via mvn exec:java)
    private static final String STATIC_DIR = "src/main/webapp";

    public static void main(String[] args) throws IOException {
        try {
            boolean connected = database.connect();
            System.out.println("Connected: " + connected);
        } catch (DLException e) {
            System.err.println(e.getMessage());
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/login",    Main::handleLogin);
        server.createContext("/api/logout",   Main::handleLogout);
        server.createContext("/api/me",       Main::handleMe);
        server.createContext("/api/cars",     Main::handleCars);
        server.createContext("/api/logs",     Main::handleLogs);
        server.createContext("/api/carimage", Main::handleCarImage);
        server.createContext("/",             Main::handleStatic);

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("AutoPrime running → http://localhost:8080");
    }

    // POST /api/login  body: username=...&password=...
    private static void handleLogin(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equals(ex.getRequestMethod())) {
                redirect(ex, "/login.html");
                return;
            }

            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("[login] body: " + body);
            Map<String, String> params = parseForm(body);
            User user = authenticate(params.get("username"), params.get("password"));
            System.out.println("[login] user found: " + (user != null ? user.getUsername() : "null"));

            if (user != null) {
                String token = UUID.randomUUID().toString();
                sessions.put(token, user);
                logAction(user.getUsername(), "LOGIN", "Role: " + user.getRole());
                String dest = "ADMIN".equals(user.getRole()) ? "/dashboard.html" : "/cars.html";
                ex.getResponseHeaders().add("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");
                redirect(ex, dest);
            } else {
                redirect(ex, "/login.html?error=1");
            }
        } catch (Exception e) {
            System.err.println("[login] ERROR: " + e.getMessage());
            e.printStackTrace();
            redirect(ex, "/login.html?error=1");
        }
    }

    // GET /api/logout
    private static void handleLogout(HttpExchange ex) throws IOException {
        String token = getCookie(ex, "session");
        if (token != null) sessions.remove(token);
        ex.getResponseHeaders().add("Set-Cookie", "session=; Path=/; Max-Age=0");
        redirect(ex, "/login.html");
    }

    // GET /api/me  → {"id":1,"username":"ivan","role":"ADMIN","fullName":"Ivan Karlo"}
    private static void handleMe(HttpExchange ex) throws IOException {
        try {
            User user = getSessionUser(ex);
            System.out.println("[me] cookie session user: " + (user != null ? user.getUsername() : "null"));
            if (user == null) {
                sendJson(ex, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            sendJson(ex, 200, userToJson(user));
        } catch (Exception e) {
            System.err.println("[me] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET    /api/cars        → JSON array of all cars
    // GET    /api/cars/{id}   → JSON single car
    // POST   /api/cars        → create car (ADMIN only), body: JSON
    // PUT    /api/cars/{id}   → update car (ADMIN only), body: JSON
    // DELETE /api/cars/{id}   → delete car (ADMIN only)
    private static void handleCars(HttpExchange ex) throws IOException {
        try {
            User user = getSessionUser(ex);

            String method = ex.getRequestMethod();
            String[] parts = ex.getRequestURI().getPath().split("/");
            boolean hasId = parts.length >= 4 && !parts[3].isEmpty();

            // Write operations — require ADMIN session
            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
                if (user == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
                if (!"ADMIN".equals(user.getRole())) {
                    sendJson(ex, 403, "{\"error\":\"Forbidden\"}");
                    return;
                }
            }

            if ("DELETE".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                boolean deleted = carDatabase.deleteCar(id);
                if (deleted) logAction(user.getUsername(), "DELETE_CAR", "Car id=" + id);
                sendJson(ex, deleted ? 200 : 404, deleted ? "{\"ok\":true}" : "{\"error\":\"Car not found\"}");

            } else if ("PUT".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Car car = parseCarJson(body);
                car.setId(id);
                validateCar(car, true);
                boolean updated = carDatabase.updateCar(car);
                if (updated) logAction(user.getUsername(), "EDIT_CAR", car.getYear() + " " + car.getMake() + " " + car.getModel() + " (id=" + id + ")");
                sendJson(ex, updated ? 200 : 404, updated ? carToJson(car) : "{\"error\":\"Car not found\"}");

            } else if ("POST".equals(method) && !hasId) {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Car car = parseCarJson(body);
                validateCar(car, false);
                boolean added = carDatabase.saveCar(car);
                if (added) logAction(user.getUsername(), "ADD_CAR", car.getYear() + " " + car.getMake() + " " + car.getModel());
                sendJson(ex, added ? 201 : 500, added ? "{\"ok\":true}" : "{\"error\":\"Could not add car\"}");

            } else if ("GET".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                Car car = carDatabase.getCarById(id);
                if (car == null) {
                    sendJson(ex, 404, "{\"error\":\"Car not found\"}");
                } else {
                    sendJson(ex, 200, carToJson(car));
                }

            } else if ("GET".equals(method)) {
                List<Car> cars = carDatabase.getAllCars();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < cars.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(carToJson(cars.get(i)));
                }
                sb.append("]");
                sendJson(ex, 200, sb.toString());

            } else {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            System.err.println("[cars] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET /api/logs → JSON array of recent action log entries (ADMIN only)
    private static void handleLogs(HttpExchange ex) throws IOException {
        try {
            User user = getSessionUser(ex);
            if (user == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!"ADMIN".equals(user.getRole())) { sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

            List<String[]> entries = actionLogDatabase.getRecent(50);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) sb.append(",");
                String[] e2 = entries.get(i);
                sb.append(String.format("{\"username\":\"%s\",\"action\":\"%s\",\"detail\":\"%s\",\"time\":\"%s\"}",
                    escapeJson(e2[0]), escapeJson(e2[1]),
                    escapeJson(e2[2] != null ? e2[2] : ""),
                    escapeJson(e2[3])));
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[logs] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET /api/carimage?make=Toyota&model=Corolla&year=2020
    // Queries Wikipedia for the best matching car image URL and returns {"url":"..."}
    private static void handleCarImage(HttpExchange ex) throws IOException {
        try {
            String query = ex.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String make  = params.getOrDefault("make",  "").trim();
            String model = params.getOrDefault("model", "").trim();
            String year  = params.getOrDefault("year",  "").trim();

            if (make.isEmpty() || model.isEmpty()) {
                sendJson(ex, 400, "{\"error\":\"make and model are required\"}");
                return;
            }

            String imageUrl = fetchWikipediaImage(make, model, year);
            if (imageUrl != null) {
                sendJson(ex, 200, "{\"url\":\"" + escapeJson(imageUrl) + "\"}");
            } else {
                sendJson(ex, 404, "{\"error\":\"No image found\"}");
            }
        } catch (Exception e) {
            System.err.println("[carimage] ERROR: " + e.getMessage());
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // Tries to find a year-specific Wikipedia car image using the search generator.
    // Searches for "YEAR MAKE MODEL" first, then falls back to "MAKE MODEL".
    // Using generator=search means we search article titles and fetch their images
    // in one request — so "2013 Toyota Camry" can match a generation-specific article.
    private static String fetchWikipediaImage(String make, String model, String year) {
        String[] queries = year.isEmpty()
            ? new String[]{ make + " " + model }
            : new String[]{ year + " " + make + " " + model, make + " " + model };

        for (String q : queries) {
            try {
                String encoded = java.net.URLEncoder.encode(q, StandardCharsets.UTF_8);
                // generator=search finds articles matching the query and returns their images —
                // more likely to hit a year/generation-specific article than a direct title lookup
                String apiUrl = "https://en.wikipedia.org/w/api.php"
                    + "?action=query"
                    + "&generator=search"
                    + "&gsrsearch=" + encoded
                    + "&gsrlimit=5"
                    + "&prop=pageimages"
                    + "&pithumbsize=1200"
                    + "&pilimit=5"
                    + "&format=json";

                java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                    java.net.URI.create(apiUrl).toURL().openConnection();
                conn.setRequestProperty("User-Agent", "AutoPrime/1.0 (car-dealership-project)");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                String resp = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();

                // Prefer a result whose thumbnail source is a photograph (not a logo/icon).
                // Wikipedia logos tend to be small PNGs; car photos tend to be JPEGs.
                // We scan all "source" values and pick the first JPEG, otherwise first any.
                String firstAny = null;
                int searchFrom = 0;
                while (true) {
                    int srcIdx = resp.indexOf("\"source\":\"", searchFrom);
                    if (srcIdx < 0) break;
                    int start = srcIdx + 10;
                    int end   = resp.indexOf('"', start);
                    if (end <= start) break;
                    String candidate = resp.substring(start, end).replace("\\/", "/");
                    searchFrom = end + 1;
                    String lower = candidate.toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.contains(".jpg/")) {
                        return candidate; // best match — JPEG photo
                    }
                    if (firstAny == null) firstAny = candidate;
                }
                if (firstAny != null) return firstAny;

            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return map;
    }

    // Serve static files from src/main/webapp/
    private static void handleStatic(HttpExchange ex) throws IOException {
        String uriPath = ex.getRequestURI().getPath();
        if ("/".equals(uriPath)) uriPath = "/index.html";

        // Block directory traversal
        Path file = Paths.get(STATIC_DIR + uriPath).normalize();
        Path base = Paths.get(STATIC_DIR).normalize();
        if (!file.startsWith(base)) {
            sendPlain(ex, 403, "Forbidden");
            return;
        }

        if (Files.exists(file) && !Files.isDirectory(file)) {
            byte[] bytes = Files.readAllBytes(file);
            ex.getResponseHeaders().add("Content-Type", contentType(uriPath));
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(bytes);
            }
        } else {
            sendPlain(ex, 404, "Not Found");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static User getSessionUser(HttpExchange ex) {
        String token = getCookie(ex, "session");
        return token != null ? sessions.get(token) : null;
    }

    private static String getCookie(HttpExchange ex, String name) {
        String header = ex.getRequestHeaders().getFirst("Cookie");
        if (header == null) return null;
        for (String part : header.split(";")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && name.equals(kv[0].trim())) return kv[1].trim();
        }
        return null;
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return map;
    }

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().add("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static void sendPlain(HttpExchange ex, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/plain");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "application/octet-stream";
    }

    private static String carToJson(Car car) {
        return String.format(
            "{\"id\":%d,\"make\":\"%s\",\"model\":\"%s\",\"year\":%d,\"price\":%.2f" +
            ",\"status\":\"%s\",\"color\":\"%s\",\"mileage\":%d,\"imageUrl\":\"%s\",\"description\":\"%s\"}",
            car.getId(),
            escapeJson(car.getMake()),
            escapeJson(car.getModel()),
            car.getYear(),
            car.getPrice(),
            escapeJson(car.getStatus() != null ? car.getStatus() : "AVAILABLE"),
            escapeJson(car.getColor() != null ? car.getColor() : ""),
            car.getMileage(),
            escapeJson(car.getImageUrl() != null ? car.getImageUrl() : ""),
            escapeJson(car.getDescription() != null ? car.getDescription() : "")
        );
    }

    private static String userToJson(User user) {
        return String.format(
            "{\"id\":%d,\"username\":\"%s\",\"role\":\"%s\",\"fullName\":\"%s\"}",
            user.getId(),
            escapeJson(user.getUsername()),
            escapeJson(user.getRole()),
            escapeJson(user.getFullName())
        );
    }

    private static Car parseCarJson(String json) {
        Car car = new Car();
        car.setMake(jsonString(json, "make"));
        car.setModel(jsonString(json, "model"));
        car.setYear(Integer.parseInt(jsonString(json, "year")));
        car.setPrice(Double.parseDouble(jsonString(json, "price")));
        String status = jsonString(json, "status");
        car.setStatus(status.isEmpty() ? "AVAILABLE" : status);
        car.setColor(jsonString(json, "color"));
        String mileageStr = jsonString(json, "mileage");
        car.setMileage(mileageStr.isEmpty() ? 0 : Integer.parseInt(mileageStr));
        car.setImageUrl(jsonString(json, "imageUrl"));
        car.setDescription(jsonString(json, "description"));
        return car;
    }

    private static String jsonString(String json, String key) {
        // handles both "key":"value" and "key":123
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int colon = json.indexOf(":", idx + search.length());
        if (colon < 0) return "";
        int start = colon + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        } else {
            int end = start;
            while (end < json.length() && ",}".indexOf(json.charAt(end)) < 0) end++;
            return json.substring(start, end).trim();
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static User authenticate(String username, String password) throws DLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        return userDatabase.authenticate(username, sha256(password));
    }

    private static String sha256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static void validateCar(Car car, boolean update) {
        int maxYear = Year.now().getValue() + 1;

        if (car == null) {
            throw new IllegalArgumentException("Car data is required.");
        }

        if (update && car.getId() <= 0) {
            throw new IllegalArgumentException("Car id is required for update.");
        }

        if (car.getMake() == null || car.getMake().trim().isEmpty()) {
            throw new IllegalArgumentException("Car make is required.");
        }

        if (car.getModel() == null || car.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Car model is required.");
        }

        if (car.getYear() < 1900 || car.getYear() > maxYear) {
            throw new IllegalArgumentException("Car year is not valid.");
        }

        if (car.getPrice() < 0) {
            throw new IllegalArgumentException("Car price cannot be negative.");
        }
    }

    private static void logAction(String username, String action, String detail) {
        try {
            actionLogDatabase.saveActionLog(username, action, detail);
        } catch (DLException e) {
            System.err.println("[action_log] Failed to write log: " + e.getMessage());
        }
    }
}
