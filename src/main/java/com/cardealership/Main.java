package com.cardealership;

import com.cardealership.database.EmployeeDatabase;
import com.cardealership.database.VehicleDatabase;
import com.cardealership.database.VehicleChangeLogDatabase;
import com.cardealership.database.ActionLogDatabase;
import com.cardealership.model.Employee;
import com.cardealership.model.Vehicle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Main HTTP server for AutoPrime.
 *
 * Refactored for Issue 1: all data access now goes through the documented
 * dealership schema entities while maintaining backward compatibility with
 * the prototype frontend.
 */
public class Main {

    private static final MySQLDatabase database = loadDatabase();

    private static MySQLDatabase loadDatabase() {
        String osUser = System.getProperty("user.name");
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Could not read db.properties.", e);
        }
        String host     = props.getProperty(osUser + ".host");
        String port     = props.getProperty(osUser + ".port");
        String dbName   = props.getProperty(osUser + ".database");
        String username = props.getProperty(osUser + ".username");
        String password = props.getProperty(osUser + ".password");
        if (host == null || password == null) {
            throw new RuntimeException("No DB config for OS user: \"" + osUser + "\".");
        }
        return new MySQLDatabase(host, port, dbName, username, password);
    }

    private static final VehicleDatabase         vehicleDatabase      = new VehicleDatabase(database);
    private static final EmployeeDatabase        employeeDatabase     = new EmployeeDatabase(database);
    private static final VehicleChangeLogDatabase vehicleLogDatabase  = new VehicleChangeLogDatabase(database);
    private static final ActionLogDatabase       actionLogDatabase    = new ActionLogDatabase(database);

    private static final Map<String, Employee> sessions = new ConcurrentHashMap<>();
    private static final String STATIC_DIR = "src/main/webapp";

    public static void main(String[] args) throws IOException {
        try {
            database.connect();
        } catch (DLException e) {
            System.err.println(e.getMessage());
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/login",    Main::handleLogin);
        server.createContext("/api/logout",   Main::handleLogout);
        server.createContext("/api/me",       Main::handleMe);
        server.createContext("/api/vehicles", Main::handleVehicles);
        server.createContext("/api/cars",     Main::handleVehicles); // Compatibility alias
        server.createContext("/api/logs",     Main::handleLogs);
        server.createContext("/api/carimage", Main::handleCarImage);
        server.createContext("/",             Main::handleStatic);

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("AutoPrime running → http://localhost:8080");
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private static void handleLogin(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equals(ex.getRequestMethod())) { redirect(ex, "/login.html"); return; }
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseForm(body);
            Employee emp = authenticate(params.get("username"), params.get("password"));

            if (emp != null) {
                String token = UUID.randomUUID().toString();
                sessions.put(token, emp);
                logSystemAction(emp.getUsername(), "LOGIN", "Role: " + emp.getRole());
                String dest = "Admin".equalsIgnoreCase(emp.getRole()) ? "/dashboard.html" : "/cars.html";
                ex.getResponseHeaders().add("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");
                redirect(ex, dest);
            } else {
                redirect(ex, "/login.html?error=1");
            }
        } catch (Exception e) {
            redirect(ex, "/login.html?error=1");
        }
    }

    private static void handleLogout(HttpExchange ex) throws IOException {
        String token = getCookie(ex, "session");
        if (token != null) {
            Employee emp = sessions.remove(token);
            if (emp != null) logSystemAction(emp.getUsername(), "LOGOUT", "");
        }
        ex.getResponseHeaders().add("Set-Cookie", "session=; Path=/; Max-Age=0");
        redirect(ex, "/login.html");
    }

    private static void handleMe(HttpExchange ex) throws IOException {
        Employee emp = getSessionEmployee(ex);
        if (emp == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
        sendJson(ex, 200, employeeToJson(emp));
    }

    private static void handleVehicles(HttpExchange ex) throws IOException {
        try {
            Employee emp = getSessionEmployee(ex);
            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();
            String[] parts = path.split("/");
            // Handle both /api/vehicles/{id} and /api/cars/{id}
            boolean hasId = parts.length >= 4 && !parts[3].isEmpty();

            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
                if (emp == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
                if (!"Admin".equalsIgnoreCase(emp.getRole())) { sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }
            }

            if ("DELETE".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                boolean ok = vehicleDatabase.deleteVehicle(id);
                if (ok) logVehicleAction(id, emp.getEmpId(), "DELETE");
                sendJson(ex, ok ? 200 : 404, ok ? "{\"ok\":true}" : "{\"error\":\"Not found\"}");

            } else if ("PUT".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Vehicle v = parseVehicleJson(body);
                v.setVehicleId(id);
                boolean ok = vehicleDatabase.updateVehicle(v);
                if (ok) logVehicleAction(id, emp.getEmpId(), "UPDATE");
                sendJson(ex, ok ? 200 : 404, ok ? vehicleToJson(v) : "{\"error\":\"Not found\"}");

            } else if ("POST".equals(method)) {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Vehicle v = parseVehicleJson(body);
                
                // Lookup or create Manufacturer and Model based on strings from frontend
                String make = jsonString(body, "make");
                String model = jsonString(body, "model");
                if (!make.isEmpty() && !model.isEmpty()) {
                    int modelId = getOrCreateRelationalData(make, model);
                    v.setModelId(modelId);
                }
                
                boolean ok = vehicleDatabase.saveVehicle(v);
                if (ok) logVehicleAction(0, emp.getEmpId(), "INSERT");
                sendJson(ex, ok ? 201 : 500, ok ? "{\"ok\":true}" : "{\"error\":\"Save failed\"}");

            } else if ("GET".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                Vehicle v = vehicleDatabase.getVehicleById(id);
                if (v == null) sendJson(ex, 404, "{\"error\":\"Not found\"}");
                else sendJson(ex, 200, vehicleToJson(v));

            } else if ("GET".equals(method)) {
                List<Vehicle> list = vehicleDatabase.getAllVehicles();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(vehicleToJson(list.get(i)));
                }
                sb.append("]");
                sendJson(ex, 200, sb.toString());
            }
        } catch (Exception e) {
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    private static void handleLogs(HttpExchange ex) throws IOException {
        try {
            Employee emp = getSessionEmployee(ex);
            if (emp == null || !"Admin".equalsIgnoreCase(emp.getRole())) {
                sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return;
            }
            // Merge vehicle logs and system logs for the dashboard
            List<String[]> entries = actionLogDatabase.getRecent(50);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) sb.append(",");
                String[] e = entries.get(i);
                sb.append(String.format("{\"username\":\"%s\",\"action\":\"%s\",\"detail\":\"%s\",\"time\":\"%s\"}",
                    escapeJson(e[0]), escapeJson(e[1]), escapeJson(e[2]), escapeJson(e[3])));
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    private static void handleCarImage(HttpExchange ex) throws IOException {
        Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());
        String make = params.getOrDefault("make", ""), model = params.getOrDefault("model", ""), year = params.getOrDefault("year", "");
        String idStr = params.getOrDefault("id", "");
        
        String url = fetchWikipediaImage(make, model, year);
        
        if (url != null && !idStr.isEmpty()) {
            try {
                vehicleDatabase.saveImage(Integer.parseInt(idStr), url);
            } catch (Exception ignored) {}
        }
        
        sendJson(ex, url != null ? 200 : 404, url != null ? "{\"url\":\"" + escapeJson(url) + "\"}" : "{\"error\":\"Not found\"}");
    }

    private static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if ("/".equals(path)) path = "/index.html";
        Path file = Paths.get(STATIC_DIR + path).normalize();
        if (!file.startsWith(Paths.get(STATIC_DIR).normalize())) { sendPlain(ex, 403, "Forbidden"); return; }
        if (Files.exists(file) && !Files.isDirectory(file)) {
            byte[] bytes = Files.readAllBytes(file);
            ex.getResponseHeaders().add("Content-Type", contentType(path));
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = ex.getResponseBody()) { out.write(bytes); }
        } else sendPlain(ex, 404, "Not Found");
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static Employee authenticate(String user, String pass) throws DLException {
        return employeeDatabase.authenticate(user, sha256(pass));
    }

    private static Employee getSessionEmployee(HttpExchange ex) {
        String token = getCookie(ex, "session");
        return token != null ? sessions.get(token) : null;
    }

    private static void logVehicleAction(int vId, int empId, String type) {
        try {
            vehicleLogDatabase.saveChangeLog(vId, empId, type);
            Employee e = employeeDatabase.getEmployeeById(empId);
            actionLogDatabase.saveActionLog(e != null ? e.getUsername() : "system", type, "Vehicle ID: " + vId);
        } catch (Exception ignored) {}
    }

    private static void logSystemAction(String user, String action, String detail) {
        try { actionLogDatabase.saveActionLog(user, action, detail); } catch (Exception ignored) {}
    }

    private static int getOrCreateRelationalData(String make, String model) {
        try {
            com.cardealership.database.ManufacturerDatabase mfrDb = new com.cardealership.database.ManufacturerDatabase(database);
            com.cardealership.database.VehicleModelDatabase modDb = new com.cardealership.database.VehicleModelDatabase(database);
            
            // 1. Get or create Manufacturer
            com.cardealership.model.Manufacturer mfr = mfrDb.getAllManufacturers().stream()
                .filter(m -> m.getName().equalsIgnoreCase(make)).findFirst().orElse(null);
            int mfrId;
            if (mfr == null) {
                mfr = new com.cardealership.model.Manufacturer(0, make);
                mfrDb.saveManufacturer(mfr);
                // Re-fetch to get the ID
                mfr = mfrDb.getAllManufacturers().stream().filter(m -> m.getName().equalsIgnoreCase(make)).findFirst().get();
            }
            mfrId = mfr.getManufacturerId();
            
            // 2. Get or create Model
            com.cardealership.model.VehicleModel vm = modDb.getAllModels().stream()
                .filter(m -> m.getModelName().equalsIgnoreCase(model) && m.getManufacturerId() == mfrId).findFirst().orElse(null);
            if (vm == null) {
                vm = new com.cardealership.model.VehicleModel(0, mfrId, model);
                modDb.saveModel(vm);
                vm = modDb.getAllModels().stream().filter(m -> m.getModelName().equalsIgnoreCase(model)).findFirst().get();
            }
            return vm.getModelId();
        } catch (Exception e) {
            return 1; // Fallback to a safe default ID if lookup fails
        }
    }

    private static String vehicleToJson(Vehicle v) {
        // Return keys matching the prototype's expected JSON format
        return String.format(
            "{\"id\":%d,\"vin\":\"%s\",\"make\":\"%s\",\"model\":\"%s\",\"year\":%d,\"price\":%.2f," +
            "\"status\":\"%s\",\"color\":\"%s\",\"mileage\":%d,\"description\":\"%s\",\"imageUrl\":\"%s\"}",
            v.getVehicleId(), escapeJson(v.getVin()), escapeJson(v.getManufacturerName()),
            escapeJson(v.getModelName()), v.getYear(), v.getPrice(), escapeJson(v.getStatus()),
            escapeJson(v.getColor()), v.getMileage(), escapeJson(v.getDescription()), escapeJson(v.getImageUrl()));
    }

    private static String employeeToJson(Employee e) {
        return String.format("{\"id\":%d,\"username\":\"%s\",\"role\":\"%s\",\"fullName\":\"%s\"}",
            e.getEmpId(), escapeJson(e.getUsername()), escapeJson(e.getRole()), escapeJson(e.getFullName()));
    }

    private static Vehicle parseVehicleJson(String json) {
        Vehicle v = new Vehicle();
        v.setVin(jsonString(json, "vin"));
        v.setYear(Integer.parseInt(jsonString(json, "year").isEmpty() ? "0" : jsonString(json, "year")));
        v.setPrice(Double.parseDouble(jsonString(json, "price").isEmpty() ? "0" : jsonString(json, "price")));
        v.setStatus(jsonString(json, "status"));
        v.setColor(jsonString(json, "color"));
        v.setMileage(Integer.parseInt(jsonString(json, "mileage").isEmpty() ? "0" : jsonString(json, "mileage")));
        v.setDescription(jsonString(json, "description"));
        // modelId must be looked up in a real app, but here we assume it's provided or handled
        String mid = jsonString(json, "modelId");
        if (!mid.isEmpty()) v.setModelId(Integer.parseInt(mid));
        return v;
    }

    private static String fetchWikipediaImage(String make, String model, String year) {
        String[] queries = { year + " " + make + " " + model, make + " " + model };
        for (String q : queries) {
            try {
                String encoded = java.net.URLEncoder.encode(q, StandardCharsets.UTF_8);
                java.net.HttpURLConnection c = (java.net.HttpURLConnection) java.net.URI.create(
                    "https://en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=" + encoded + 
                    "&gsrlimit=3&prop=pageimages&pithumbsize=1000&format=json").toURL().openConnection();
                c.setRequestProperty("User-Agent", "AutoPrime/1.0");
                c.setConnectTimeout(3000);
                String r = new String(c.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                
                // Simple but more reliable parsing for "source":"..."
                int start = 0;
                while ((start = r.indexOf("\"source\":\"", start)) != -1) {
                    start += 10;
                    int end = r.indexOf("\"", start);
                    String url = r.substring(start, end).replace("\\/", "/");
                    // Filter out logos/icons - usually we want JPEGs for car photos
                    if (url.toLowerCase().contains(".jpg") || url.toLowerCase().contains(".jpeg")) {
                        return url;
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static void redirect(HttpExchange ex, String loc) throws IOException {
        ex.getResponseHeaders().add("Location", loc);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] b = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, b.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(b); }
    }

    private static void sendPlain(HttpExchange ex, int status, String text) throws IOException {
        byte[] b = text.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, b.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(b); }
    }

    private static String contentType(String p) {
        if (p.endsWith(".html")) return "text/html";
        if (p.endsWith(".css")) return "text/css";
        if (p.endsWith(".js")) return "application/javascript";
        return "application/octet-stream";
    }

    private static String getCookie(HttpExchange ex, String n) {
        String h = ex.getRequestHeaders().getFirst("Cookie");
        if (h == null) return null;
        for (String p : h.split(";")) {
            String[] kv = p.trim().split("=", 2);
            if (kv.length == 2 && n.equals(kv[0])) return kv[1];
        }
        return null;
    }

    private static Map<String, String> parseForm(String b) {
        Map<String, String> m = new HashMap<>();
        for (String p : b.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) m.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return m;
    }

    private static Map<String, String> parseQuery(String q) {
        Map<String, String> m = new HashMap<>();
        if (q != null) for (String p : q.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) m.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
        }
        return m;
    }

    private static String jsonString(String j, String k) {
        int i = j.indexOf("\"" + k + "\"");
        if (i < 0) return "";
        int c = j.indexOf(":", i);
        int s = j.indexOf("\"", c);
        if (s > 0 && s < j.indexOf(",", c) && s < j.indexOf("}", c)) {
            return j.substring(s + 1, j.indexOf("\"", s + 1));
        }
        int e = j.indexOf(",", c);
        if (e < 0) e = j.indexOf("}", c);
        return j.substring(c + 1, e).trim();
    }

    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    private static String sha256(String i) {
        try {
            java.security.MessageDigest m = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h = m.digest(i.getBytes(StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            for (byte b : h) s.append(String.format("%02x", b));
            return s.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
