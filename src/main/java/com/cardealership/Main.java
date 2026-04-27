package com.cardealership;

import com.cardealership.database.ActionLogDatabase;
import com.cardealership.database.CarDatabase;
import com.cardealership.database.CustomerDatabase;
import com.cardealership.database.EmployeeDatabase;
import com.cardealership.database.EnquiryDatabase;
import com.cardealership.database.ReviewDatabase;
import com.cardealership.database.SalesDatabase;
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
        String params   = props.getProperty(osUser + ".params", "");
        if (host == null || password == null) {
            throw new RuntimeException("No DB config found for OS user: \"" + osUser + "\". Add your entry to db.properties.");
        }
        return new MySQLDatabase(host, port, database, username, password, params);
    }
    private static final Map<String, User> sessions = new ConcurrentHashMap<>();
    private static final CarDatabase carDatabase = new CarDatabase(database);
    private static final UserDatabase userDatabase = new UserDatabase(database);
    private static final ActionLogDatabase actionLogDatabase = new ActionLogDatabase(database);
    private static final ReviewDatabase reviewDatabase = new ReviewDatabase(database);
    private static final EnquiryDatabase enquiryDatabase = new EnquiryDatabase(database);
    private static final CustomerDatabase customerDatabase = new CustomerDatabase(database);
    private static final EmployeeDatabase employeeDatabase = new EmployeeDatabase(database);
    private static final SalesDatabase salesDatabase = new SalesDatabase(database);


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
        server.createContext("/api/logs",       Main::handleLogs);
        server.createContext("/api/carimage",   Main::handleCarImage);
        server.createContext("/api/models",     Main::handleModels);
        server.createContext("/api/register",   Main::handleRegister);
        server.createContext("/api/reviews",    Main::handleReviews);
        server.createContext("/api/enquiry",    Main::handleEnquiry);
        server.createContext("/api/enquiries",  Main::handleEnquiries);
        server.createContext("/api/sales",      Main::handleSales);
        server.createContext("/api/customers",  Main::handleCustomers);
        server.createContext("/api/employees",  Main::handleEmployees);
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
                System.out.println("[login] " + user.getUsername() + " logged in, role=" + user.getRole());
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

    // GET /api/me  → {"id":1,"username":"ivan","role":"ADMIN"[,"firstName":...,"lastName":...,"email":...]}
    private static void handleMe(HttpExchange ex) throws IOException {
        try {
            User user = getSessionUser(ex);
            System.out.println("[me] cookie session user: " + (user != null ? user.getUsername() : "null"));
            if (user == null) {
                sendJson(ex, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }
            String json = userToJson(user);
            if ("CUSTOMER".equals(user.getRole()) && user.getCustomerId() > 0) {
                String[] cust = customerDatabase.getCustomerById(user.getCustomerId());
                if (cust != null) {
                    // columns: customer_id(0) first_name(1) last_name(2) email(3) phone(4)
                    json = json.substring(0, json.length() - 1)
                        + ",\"firstName\":\"" + escapeJson(cust[1] != null ? cust[1] : "") + "\""
                        + ",\"lastName\":\"" + escapeJson(cust[2] != null ? cust[2] : "") + "\""
                        + ",\"email\":\"" + escapeJson(cust[3] != null ? cust[3] : "") + "\""
                        + "}";
                }
            }
            sendJson(ex, 200, json);
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
            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method)) {
                if (user == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
                if (!"ADMIN".equals(user.getRole())) {
                    sendJson(ex, 403, "{\"error\":\"Forbidden\"}");
                    return;
                }
            }

            if ("DELETE".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                boolean deleted = carDatabase.deleteCar(id);
                if (deleted) logVehicleChange(id, user.getEmpId(), "DELETE", null, null);
                sendJson(ex, deleted ? 200 : 404, deleted ? "{\"ok\":true}" : "{\"error\":\"Car not found\"}");

            } else if ("PATCH".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String status = jsonString(body, "status");
                if (status.isBlank()) { sendJson(ex, 400, "{\"error\":\"status is required\"}"); return; }
                String dbStatus = toDatabaseStatus(status);
                boolean updated = carDatabase.updateCarStatus(id, dbStatus);
                if (updated) logVehicleChange(id, user.getEmpId(), "UPDATE", "status", dbStatus);
                sendJson(ex, updated ? 200 : 404, updated ? "{\"ok\":true,\"status\":\"" + toApiStatus(dbStatus) + "\"}" : "{\"error\":\"Car not found\"}");

            } else if ("PUT".equals(method) && hasId) {
                int id = Integer.parseInt(parts[3]);
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Car car = parseCarJson(body);
                car.setId(id);
                if (car.getModelId() <= 0 && car.getMake() != null && !car.getMake().isBlank()
                        && car.getModel() != null && !car.getModel().isBlank()) {
                    car.setModelId(carDatabase.findOrCreateModelId(
                        car.getMake(), car.getModel(), car.getBodyType(), car.getSegment(), car.getCountry()));
                }
                validateCar(car, true);
                boolean updated = carDatabase.updateCar(car);
                if (updated) {
                    logVehicleChange(id, user.getEmpId(), "UPDATE", "vehicle", String.valueOf(id));
                    if (car.getImageUrl() != null && !car.getImageUrl().isBlank()) {
                        carDatabase.upsertVehicleImage(id, car.getImageUrl());
                    }
                }
                sendJson(ex, updated ? 200 : 404, updated ? carToJson(car) : "{\"error\":\"Car not found\"}");

            } else if ("POST".equals(method) && !hasId) {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Car car = parseCarJson(body);
                if (car.getModelId() <= 0 && car.getMake() != null && !car.getMake().isBlank()
                        && car.getModel() != null && !car.getModel().isBlank()) {
                    car.setModelId(carDatabase.findOrCreateModelId(
                        car.getMake(), car.getModel(), car.getBodyType(), car.getSegment(), car.getCountry()));
                }
                if (car.getVin() == null || car.getVin().isBlank()) {
                    car.setVin(carDatabase.generateUniqueVin());
                }
                validateCar(car, false);
                boolean added = carDatabase.saveCar(car);
                if (added) logVehicleChange(car.getModelId(), user.getEmpId(), "INSERT", null, null);
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
                // columns: vehicle_id(0), first_name(1), last_name(2), change_type(3), field_changed(4), new_value(5), change_date(6)
                sb.append(String.format(
                    "{\"vehicleId\":\"%s\",\"employee\":\"%s %s\",\"changeType\":\"%s\",\"fieldChanged\":\"%s\",\"newValue\":\"%s\",\"time\":\"%s\"}",
                    escapeJson(e2[0] != null ? e2[0] : ""),
                    escapeJson(e2[1] != null ? e2[1] : ""),
                    escapeJson(e2[2] != null ? e2[2] : ""),
                    escapeJson(e2[3] != null ? e2[3] : ""),
                    escapeJson(e2[4] != null ? e2[4] : ""),
                    escapeJson(e2[5] != null ? e2[5] : ""),
                    escapeJson(e2[6] != null ? e2[6] : "")));
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[logs] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET /api/carimage?make=Toyota&model=Corolla&year=2020[&vehicleId=5]
    private static void handleCarImage(HttpExchange ex) throws IOException {
        try {
            String query = ex.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String make      = params.getOrDefault("make",  "").trim();
            String model     = params.getOrDefault("model", "").trim();
            String year      = params.getOrDefault("year",  "").trim();
            String vehicleIdStr = params.getOrDefault("vehicleId", "").trim();

            if (make.isEmpty() || model.isEmpty()) {
                sendJson(ex, 400, "{\"error\":\"make and model are required\"}");
                return;
            }

            // 1. Check DB cache first
            String imageUrl = null;
            if (!vehicleIdStr.isEmpty()) {
                try {
                    imageUrl = carDatabase.getVehicleImageUrl(Integer.parseInt(vehicleIdStr));
                } catch (Exception ignored) {}
            }

            // 2. Wikipedia fallback
            if (imageUrl == null) {
                imageUrl = fetchWikipediaImage(make, model, year);
            }

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

    // POST /api/register  body: JSON {username, password, firstName, lastName, email, phone}  (public)
    private static void handleRegister(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body      = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String username  = jsonString(body, "username").trim();
            String password  = jsonString(body, "password").trim();
            String firstName = jsonString(body, "firstName").trim();
            String lastName  = jsonString(body, "lastName").trim();
            String email     = jsonString(body, "email").trim();
            String phone     = jsonString(body, "phone").trim();

            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                sendJson(ex, 400, "{\"error\":\"All fields are required\"}");
                return;
            }
            if (!email.contains("@")) {
                sendJson(ex, 400, "{\"error\":\"Invalid email address\"}");
                return;
            }
            if (userDatabase.getUserByUsername(username) != null) {
                sendJson(ex, 409, "{\"error\":\"Username already taken\"}");
                return;
            }
            if (customerDatabase.emailExists(email)) {
                sendJson(ex, 409, "{\"error\":\"An account with this email already exists\"}");
                return;
            }

            int customerId = customerDatabase.createCustomer(firstName, lastName, email, phone);
            if (customerId <= 0) {
                sendJson(ex, 500, "{\"error\":\"Could not create customer record\"}");
                return;
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(sha256(password));
            newUser.setRole("CUSTOMER");
            newUser.setCustomerId(customerId);

            boolean saved = userDatabase.saveUser(newUser);
            if (!saved) {
                sendJson(ex, 500, "{\"error\":\"Could not create user account\"}");
                return;
            }

            // Log the new user in immediately
            User created = userDatabase.authenticate(username, sha256(password));
            if (created != null) {
                String token = UUID.randomUUID().toString();
                sessions.put(token, created);
                ex.getResponseHeaders().add("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");
            }
            sendJson(ex, 201, "{\"ok\":true}");
        } catch (Exception e) {
            System.err.println("[register] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // POST /api/enquiry  body: JSON {vehicleId, name, email, phone, message}  (requires CUSTOMER session)
    private static void handleEnquiry(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            User user = getSessionUser(ex);
            if (user == null || !"CUSTOMER".equals(user.getRole())) {
                sendJson(ex, 401, "{\"error\":\"Please sign in as a customer to submit an enquiry\"}");
                return;
            }

            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String name    = jsonString(body, "name").trim();
            String email   = jsonString(body, "email").trim();
            String phone   = jsonString(body, "phone").trim();
            String message = jsonString(body, "message").trim();
            String vidStr  = jsonString(body, "vehicleId").trim();

            if (name.isEmpty() || email.isEmpty()) {
                sendJson(ex, 400, "{\"error\":\"name and email are required\"}");
                return;
            }
            if (!email.contains("@")) {
                sendJson(ex, 400, "{\"error\":\"invalid email address\"}");
                return;
            }

            int vehicleId = 0;
            try { vehicleId = Integer.parseInt(vidStr); } catch (NumberFormatException ignored) {}

            boolean saved = enquiryDatabase.saveEnquiry(vehicleId, user.getCustomerId(), name, email, phone, message);
            sendJson(ex, saved ? 201 : 500, saved ? "{\"ok\":true}" : "{\"error\":\"Could not save enquiry\"}");
        } catch (Exception e) {
            System.err.println("[enquiry] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET /api/enquiries  (ADMIN only) → recent 50 enquiries as JSON array
    private static void handleEnquiries(HttpExchange ex) throws IOException {
        try {
            User user = getSessionUser(ex);
            if (user == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!"ADMIN".equals(user.getRole())) { sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                String query = ex.getRequestURI().getQuery();
                int enquiryId = 0;
                boolean read = true;
                if (query != null) {
                    for (String part : query.split("&")) {
                        if (part.startsWith("id=")) {
                            try { enquiryId = Integer.parseInt(part.substring(3)); } catch (NumberFormatException ignored) {}
                        } else if (part.startsWith("read=")) {
                            read = !"false".equalsIgnoreCase(part.substring(5));
                        }
                    }
                }
                if (enquiryId <= 0) { sendJson(ex, 400, "{\"error\":\"Missing id\"}"); return; }
                enquiryDatabase.markRead(enquiryId, read);
                sendJson(ex, 200, "{\"ok\":true}");
                return;
            }

            String[][] rows = enquiryDatabase.getRecent(50);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 1; i < rows.length; i++) {   // row 0 is the header from getData()
                if (i > 1) sb.append(",");
                String[] r = rows[i];
                // columns: enquiry_id(0) name(1) email(2) phone(3) message(4) submitted_at(5) is_read(6) vehicle_label(7) customer_id(8) vehicle_id(9)
                sb.append(String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"message\":\"%s\",\"time\":\"%s\",\"isRead\":%s,\"vehicle\":\"%s\",\"customerId\":\"%s\",\"vehicleId\":\"%s\"}",
                    escapeJson(r[0] != null ? r[0] : ""),
                    escapeJson(r[1] != null ? r[1] : ""),
                    escapeJson(r[2] != null ? r[2] : ""),
                    escapeJson(r[3] != null ? r[3] : ""),
                    escapeJson(r[4] != null ? r[4] : ""),
                    escapeJson(r[5] != null ? r[5] : ""),
                    "1".equals(r[6]) || "true".equalsIgnoreCase(r[6]) ? "true" : "false",
                    escapeJson(r[7] != null ? r[7] : "General"),
                    escapeJson(r[8] != null ? r[8] : ""),
                    escapeJson(r[9] != null ? r[9] : "")
                ));
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[enquiries] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // POST /api/sales  (ADMIN only) — record a sale
    // GET  /api/sales  (ADMIN only) — list recent sales
    private static void handleSales(HttpExchange ex) throws IOException {
        try {
            User user = getSessionUser(ex);
            if (user == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!"ADMIN".equals(user.getRole())) { sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

            if ("POST".equals(ex.getRequestMethod())) {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String vehicleIdStr  = jsonString(body, "vehicleId").trim();
                String customerIdStr = jsonString(body, "customerId").trim();
                String salePriceStr  = jsonString(body, "salePrice").trim();
                String payment       = jsonString(body, "paymentMethod").trim();
                String notes         = jsonString(body, "notes").trim();

                if (vehicleIdStr.isEmpty() || customerIdStr.isEmpty() || salePriceStr.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"vehicleId, customerId, and salePrice are required\"}");
                    return;
                }

                int vehicleId  = Integer.parseInt(vehicleIdStr);
                int customerId = Integer.parseInt(customerIdStr);
                double salePrice = Double.parseDouble(salePriceStr);
                if (payment.isEmpty()) payment = "CASH";

                boolean ok = salesDatabase.recordSale(vehicleId, customerId, user.getEmpId(), salePrice, payment, notes);
                sendJson(ex, ok ? 201 : 500, ok ? "{\"ok\":true}" : "{\"error\":\"Could not record sale\"}");

            } else if ("GET".equals(ex.getRequestMethod())) {
                String[][] rows = salesDatabase.getRecentSales(50);
                StringBuilder sb = new StringBuilder("[");
                // header row at index 0 from getData(); data starts at 1
                for (int i = 1; i < rows.length; i++) {
                    if (i > 1) sb.append(",");
                    String[] r = rows[i];
                    // sale_id(0) sale_price(1) payment_method(2) sale_date(3) notes(4) vehicle_label(5) customer_name(6) customer_email(7)
                    sb.append(String.format(
                        "{\"saleId\":\"%s\",\"salePrice\":\"%s\",\"paymentMethod\":\"%s\",\"saleDate\":\"%s\",\"notes\":\"%s\",\"vehicle\":\"%s\",\"customer\":\"%s\",\"customerEmail\":\"%s\"}",
                        escapeJson(r[0] != null ? r[0] : ""),
                        escapeJson(r[1] != null ? r[1] : ""),
                        escapeJson(r[2] != null ? r[2] : ""),
                        escapeJson(r[3] != null ? r[3] : ""),
                        escapeJson(r[4] != null ? r[4] : ""),
                        escapeJson(r[5] != null ? r[5] : ""),
                        escapeJson(r[6] != null ? r[6] : ""),
                        escapeJson(r[7] != null ? r[7] : "")
                    ));
                }
                sb.append("]");
                sendJson(ex, 200, sb.toString());

            } else {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            System.err.println("[sales] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET /api/customers  (ADMIN only) → list all customers as JSON array
    private static void handleCustomers(HttpExchange ex) throws IOException {
        try {
            User user = getSessionUser(ex);
            if (user == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!"ADMIN".equals(user.getRole())) { sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String[][] rows = customerDatabase.getAllCustomers();
            StringBuilder sb = new StringBuilder("[");
            // header row at index 0; data starts at 1
            for (int i = 1; i < rows.length; i++) {
                if (i > 1) sb.append(",");
                String[] r = rows[i];
                // customer_id(0) first_name(1) last_name(2) email(3)
                sb.append(String.format(
                    "{\"customerId\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\"}",
                    escapeJson(r[0] != null ? r[0] : ""),
                    escapeJson(r[1] != null ? r[1] : ""),
                    escapeJson(r[2] != null ? r[2] : ""),
                    escapeJson(r[3] != null ? r[3] : "")
                ));
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[customers] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET /api/employees  (ADMIN only) → list all employees as JSON array
    private static void handleEmployees(HttpExchange ex) throws IOException {
        try {
            User user = getSessionUser(ex);
            if (user == null) { sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!"ADMIN".equals(user.getRole())) { sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String[][] rows = employeeDatabase.getAllEmployees();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 1; i < rows.length; i++) {
                if (i > 1) sb.append(",");
                String[] r = rows[i];
                sb.append(String.format(
                    "{\"employeeId\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"hireDate\":\"%s\",\"isActive\":%s,\"username\":\"%s\",\"role\":\"%s\"}",
                    escapeJson(r[0] != null ? r[0] : ""),
                    escapeJson(r[1] != null ? r[1] : ""),
                    escapeJson(r[2] != null ? r[2] : ""),
                    escapeJson(r[3] != null ? r[3] : ""),
                    escapeJson(r[4] != null ? r[4] : ""),
                    escapeJson(r[5] != null ? r[5] : ""),
                    "1".equals(r[6]) || "true".equalsIgnoreCase(r[6]) ? "true" : "false",
                    escapeJson(r[7] != null ? r[7] : ""),
                    escapeJson(r[8] != null ? r[8] : "EMPLOYEE")
                ));
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[employees] ERROR: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET /api/models/{id}/reviews
    private static void handleModels(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            // expected: /api/models/{id}/reviews
            String[] parts = path.split("/");
            if (parts.length < 5 || !"reviews".equals(parts[4])) {
                sendJson(ex, 404, "{\"error\":\"Not found\"}");
                return;
            }
            int modelId = Integer.parseInt(parts[3]);
            double avg = reviewDatabase.getAverageRating(modelId);
            java.util.List<java.util.Map<String, Object>> reviews = reviewDatabase.getReviewsByModelId(modelId);

            StringBuilder sb = new StringBuilder();
            sb.append("{\"averageRating\":").append(String.format("%.2f", avg));
            sb.append(",\"reviews\":[");
            for (int i = 0; i < reviews.size(); i++) {
                if (i > 0) sb.append(",");
                java.util.Map<String, Object> r = reviews.get(i);
                sb.append("{")
                  .append("\"reviewId\":").append(r.get("reviewId")).append(",")
                  .append("\"authorName\":\"").append(escapeJson((String) r.get("authorName"))).append("\",")
                  .append("\"rating\":").append(r.get("rating")).append(",")
                  .append("\"reviewText\":\"").append(escapeJson((String) r.get("reviewText"))).append("\",")
                  .append("\"source\":\"").append(escapeJson((String) r.get("source"))).append("\",")
                  .append("\"createdAt\":\"").append(escapeJson((String) r.get("createdAt"))).append("\"")
                  .append("}");
            }
            sb.append("]}");
            sendJson(ex, 200, sb.toString());
        } catch (NumberFormatException e) {
            sendJson(ex, 400, "{\"error\":\"Invalid model id\"}");
        } catch (Exception e) {
            System.err.println("[models] ERROR: " + e.getMessage());
            sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    // GET /api/reviews  → JSON array of all reviews (public)
    private static void handleReviews(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equals(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            java.util.List<java.util.Map<String, Object>> reviews = reviewDatabase.getAllReviews();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < reviews.size(); i++) {
                if (i > 0) sb.append(",");
                java.util.Map<String, Object> r = reviews.get(i);
                sb.append("{")
                  .append("\"reviewId\":").append(r.get("reviewId")).append(",")
                  .append("\"authorName\":\"").append(escapeJson((String) r.get("authorName"))).append("\",")
                  .append("\"rating\":").append(r.get("rating")).append(",")
                  .append("\"reviewText\":\"").append(escapeJson((String) r.get("reviewText"))).append("\",")
                  .append("\"source\":\"").append(escapeJson((String) r.get("source"))).append("\",")
                  .append("\"createdAt\":\"").append(escapeJson((String) r.get("createdAt"))).append("\",")
                  .append("\"modelName\":\"").append(escapeJson((String) r.get("modelName"))).append("\",")
                  .append("\"manufacturerName\":\"").append(escapeJson((String) r.get("manufacturerName"))).append("\"")
                  .append("}");
            }
            sb.append("]");
            sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[reviews] ERROR: " + e.getMessage());
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
            ",\"status\":\"%s\",\"color\":\"%s\",\"mileage\":%d,\"imageUrl\":\"%s\"" +
            ",\"description\":\"%s\",\"vin\":\"%s\",\"modelId\":%d,\"manufacturerId\":%d" +
            ",\"segment\":\"%s\"}",
            car.getId(),
            escapeJson(car.getMake()),
            escapeJson(car.getModel()),
            car.getYear(),
            car.getPrice(),
            escapeJson(toApiStatus(car.getStatus())),
            escapeJson(car.getColor() != null ? car.getColor() : ""),
            car.getMileage(),
            escapeJson(car.getImageUrl() != null ? car.getImageUrl() : ""),
            escapeJson(car.getDescription() != null ? car.getDescription() : ""),
            escapeJson(car.getVin() != null ? car.getVin() : ""),
            car.getModelId(),
            car.getManufacturerId(),
            escapeJson(car.getSegment() != null ? car.getSegment() : "")
        );
    }

    private static String userToJson(User user) {
        return String.format(
            "{\"id\":%d,\"username\":\"%s\",\"role\":\"%s\",\"empId\":%d,\"customerId\":%d}",
            user.getId(),
            escapeJson(user.getUsername()),
            escapeJson(user.getRole()),
            user.getEmpId(),
            user.getCustomerId()
        );
    }

    private static Car parseCarJson(String json) {
        Car car = new Car();
        car.setMake(jsonString(json, "make"));
        car.setModel(jsonString(json, "model"));
        String modelIdStr = jsonString(json, "modelId");
        car.setModelId(modelIdStr.isEmpty() ? 0 : Integer.parseInt(modelIdStr));
        String yearStr = jsonString(json, "year");
        car.setYear(yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr));
        String priceStr = jsonString(json, "price");
        car.setPrice(priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr));
        String status = jsonString(json, "status");
        car.setStatus(toDatabaseStatus(status));
        car.setColor(jsonString(json, "color"));
        String mileageStr = jsonString(json, "mileage");
        car.setMileage(mileageStr.isEmpty() ? 0 : Integer.parseInt(mileageStr));
        car.setVin(jsonString(json, "vin"));
        car.setImageUrl(jsonString(json, "imageUrl"));
        car.setDescription(jsonString(json, "description"));
        car.setBodyType(jsonString(json, "bodyType"));
        car.setSegment(jsonString(json, "segment"));
        car.setCountry(jsonString(json, "country"));
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

    private static String toApiStatus(String status) {
        if (status == null || status.isBlank()) return "AVAILABLE";
        return switch (status.trim().toLowerCase()) {
            case "sold" -> "SOLD";
            case "reserved" -> "RESERVED";
            default -> "AVAILABLE";
        };
    }

    private static String toDatabaseStatus(String status) {
        if (status == null || status.isBlank()) return "Available";
        return switch (status.trim().toUpperCase()) {
            case "SOLD" -> "Sold";
            case "RESERVED" -> "Reserved";
            default -> "Available";
        };
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

        if (car.getModelId() <= 0) {
            throw new IllegalArgumentException("modelId is required.");
        }

        if (car.getYear() < 1900 || car.getYear() > maxYear) {
            throw new IllegalArgumentException("Car year is not valid.");
        }

        if (car.getPrice() < 0) {
            throw new IllegalArgumentException("Car price cannot be negative.");
        }
    }

    private static void logVehicleChange(int vehicleId, int empId, String changeType, String fieldChanged, String newValue) {
        try {
            actionLogDatabase.saveActionLog(vehicleId, empId, changeType, fieldChanged, newValue);
        } catch (DLException e) {
            System.err.println("[change_log] Failed to write log: " + e.getMessage());
        }
    }
}
