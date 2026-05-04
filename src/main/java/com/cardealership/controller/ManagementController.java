package com.cardealership.controller;

import com.cardealership.AppContext;
import com.cardealership.model.User;
import com.cardealership.service.AuthService;
import com.cardealership.util.HttpUtil;
import com.cardealership.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 * Groups admin/staff-oriented management endpoints to keep Main.java focused on
 * startup only.
 */
public class ManagementController {

    private final AppContext context;
    private final AuthService authService;

    public ManagementController(AppContext context, AuthService authService) {
        this.context = context;
        this.authService = authService;
    }

    public void handleEnquiry(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equals(ex.getRequestMethod())) {
                HttpUtil.sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            User user = authService.getSessionUser(ex);
            if (user == null || !"CUSTOMER".equals(user.getRole())) {
                HttpUtil.sendJson(ex, 401, "{\"error\":\"Please sign in as a customer to submit an enquiry\"}");
                return;
            }

            String body = HttpUtil.readBody(ex);
            String name = JsonUtil.jsonString(body, "name").trim();
            String email = JsonUtil.jsonString(body, "email").trim();
            String phone = JsonUtil.jsonString(body, "phone").trim();
            String message = JsonUtil.jsonString(body, "message").trim();
            String vidStr = JsonUtil.jsonString(body, "vehicleId").trim();

            if (name.isEmpty() || email.isEmpty()) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"name and email are required\"}");
                return;
            }
            if (!email.contains("@")) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"invalid email address\"}");
                return;
            }

            int vehicleId = 0;
            try { vehicleId = Integer.parseInt(vidStr); } catch (NumberFormatException ignored) {}

            boolean saved = context.enquiryDatabase.saveEnquiry(vehicleId, user.getCustomerId(), name, email, phone, message);
            HttpUtil.sendJson(ex, saved ? 201 : 500, saved ? "{\"ok\":true}" : "{\"error\":\"Could not save enquiry\"}");
        } catch (Exception e) {
            System.err.println("[enquiry] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleEnquiries(HttpExchange ex) throws IOException {
        try {
            User user = authService.getSessionUser(ex);
            if (user == null) { HttpUtil.sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!authService.canAccessStaffFeatures(user)) { HttpUtil.sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

            if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                int enquiryId = 0;
                boolean read = true;
                String query = ex.getRequestURI().getQuery();
                if (query != null) {
                    for (String part : query.split("&")) {
                        if (part.startsWith("id=")) {
                            try { enquiryId = Integer.parseInt(part.substring(3)); } catch (NumberFormatException ignored) {}
                        } else if (part.startsWith("read=")) {
                            read = !"false".equalsIgnoreCase(part.substring(5));
                        }
                    }
                }
                if (enquiryId <= 0) { HttpUtil.sendJson(ex, 400, "{\"error\":\"Missing id\"}"); return; }
                context.enquiryDatabase.markRead(enquiryId, read);
                HttpUtil.sendJson(ex, 200, "{\"ok\":true}");
                return;
            }

            String[][] rows = context.enquiryDatabase.getRecent(50);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 1; i < rows.length; i++) {
                if (i > 1) sb.append(",");
                String[] row = rows[i];
                sb.append(String.format(
                    "{\"id\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"message\":\"%s\",\"time\":\"%s\",\"isRead\":%s,\"vehicle\":\"%s\",\"customerId\":\"%s\",\"vehicleId\":\"%s\"}",
                    JsonUtil.escapeJson(row[0] != null ? row[0] : ""),
                    JsonUtil.escapeJson(row[1] != null ? row[1] : ""),
                    JsonUtil.escapeJson(row[2] != null ? row[2] : ""),
                    JsonUtil.escapeJson(row[3] != null ? row[3] : ""),
                    JsonUtil.escapeJson(row[4] != null ? row[4] : ""),
                    JsonUtil.escapeJson(row[5] != null ? row[5] : ""),
                    "1".equals(row[6]) || "true".equalsIgnoreCase(row[6]) ? "true" : "false",
                    JsonUtil.escapeJson(row[7] != null ? row[7] : "General"),
                    JsonUtil.escapeJson(row[8] != null ? row[8] : ""),
                    JsonUtil.escapeJson(row[9] != null ? row[9] : "")
                ));
            }
            sb.append("]");
            HttpUtil.sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[enquiries] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleSales(HttpExchange ex) throws IOException {
        try {
            User user = authService.getSessionUser(ex);
            if (user == null) { HttpUtil.sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!authService.canAccessStaffFeatures(user)) { HttpUtil.sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }

            if ("POST".equals(ex.getRequestMethod())) {
                String body = HttpUtil.readBody(ex);
                String vehicleIdStr = JsonUtil.jsonString(body, "vehicleId").trim();
                String customerIdStr = JsonUtil.jsonString(body, "customerId").trim();
                String salePriceStr = JsonUtil.jsonString(body, "salePrice").trim();
                String payment = JsonUtil.jsonString(body, "paymentMethod").trim();
                String notes = JsonUtil.jsonString(body, "notes").trim();

                if (vehicleIdStr.isEmpty() || customerIdStr.isEmpty() || salePriceStr.isEmpty()) {
                    HttpUtil.sendJson(ex, 400, "{\"error\":\"vehicleId, customerId, and salePrice are required\"}");
                    return;
                }

                int vehicleId = Integer.parseInt(vehicleIdStr);
                int customerId = Integer.parseInt(customerIdStr);
                double salePrice = Double.parseDouble(salePriceStr);
                if (payment.isEmpty()) payment = "CASH";

                boolean ok = context.salesDatabase.recordSale(vehicleId, customerId, user.getEmpId(), salePrice, payment, notes);
                HttpUtil.sendJson(ex, ok ? 201 : 500, ok ? "{\"ok\":true}" : "{\"error\":\"Could not record sale\"}");
            } else if ("GET".equals(ex.getRequestMethod())) {
                String[][] rows = context.salesDatabase.getRecentSales(50);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 1; i < rows.length; i++) {
                    if (i > 1) sb.append(",");
                    String[] row = rows[i];
                    sb.append(String.format(
                        "{\"saleId\":\"%s\",\"salePrice\":\"%s\",\"paymentMethod\":\"%s\",\"saleDate\":\"%s\",\"notes\":\"%s\",\"vehicle\":\"%s\",\"customer\":\"%s\",\"customerEmail\":\"%s\"}",
                        JsonUtil.escapeJson(row[0] != null ? row[0] : ""),
                        JsonUtil.escapeJson(row[1] != null ? row[1] : ""),
                        JsonUtil.escapeJson(row[2] != null ? row[2] : ""),
                        JsonUtil.escapeJson(row[3] != null ? row[3] : ""),
                        JsonUtil.escapeJson(row[4] != null ? row[4] : ""),
                        JsonUtil.escapeJson(row[5] != null ? row[5] : ""),
                        JsonUtil.escapeJson(row[6] != null ? row[6] : ""),
                        JsonUtil.escapeJson(row[7] != null ? row[7] : "")
                    ));
                }
                sb.append("]");
                HttpUtil.sendJson(ex, 200, sb.toString());
            } else {
                HttpUtil.sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            System.err.println("[sales] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleCustomers(HttpExchange ex) throws IOException {
        try {
            User user = authService.getSessionUser(ex);
            if (user == null) { HttpUtil.sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!authService.canAccessStaffFeatures(user)) { HttpUtil.sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }
            if (!"GET".equals(ex.getRequestMethod())) {
                HttpUtil.sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String[][] rows = context.customerDatabase.getAllCustomers();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 1; i < rows.length; i++) {
                if (i > 1) sb.append(",");
                String[] row = rows[i];
                sb.append(String.format(
                    "{\"customerId\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\"}",
                    JsonUtil.escapeJson(row[0] != null ? row[0] : ""),
                    JsonUtil.escapeJson(row[1] != null ? row[1] : ""),
                    JsonUtil.escapeJson(row[2] != null ? row[2] : ""),
                    JsonUtil.escapeJson(row[3] != null ? row[3] : "")
                ));
            }
            sb.append("]");
            HttpUtil.sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[customers] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleEmployees(HttpExchange ex) throws IOException {
        try {
            User user = authService.getSessionUser(ex);
            if (user == null) { HttpUtil.sendJson(ex, 401, "{\"error\":\"Unauthorized\"}"); return; }
            if (!authService.canAccessStaffFeatures(user)) { HttpUtil.sendJson(ex, 403, "{\"error\":\"Forbidden\"}"); return; }
            if (!"GET".equals(ex.getRequestMethod())) {
                HttpUtil.sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String[][] rows = context.employeeDatabase.getAllEmployees();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 1; i < rows.length; i++) {
                if (i > 1) sb.append(",");
                String[] row = rows[i];
                sb.append(String.format(
                    "{\"employeeId\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"hireDate\":\"%s\",\"isActive\":%s,\"username\":\"%s\",\"role\":\"%s\"}",
                    JsonUtil.escapeJson(row[0] != null ? row[0] : ""),
                    JsonUtil.escapeJson(row[1] != null ? row[1] : ""),
                    JsonUtil.escapeJson(row[2] != null ? row[2] : ""),
                    JsonUtil.escapeJson(row[3] != null ? row[3] : ""),
                    JsonUtil.escapeJson(row[4] != null ? row[4] : ""),
                    JsonUtil.escapeJson(row[5] != null ? row[5] : ""),
                    "1".equals(row[6]) || "true".equalsIgnoreCase(row[6]) ? "true" : "false",
                    JsonUtil.escapeJson(row[7] != null ? row[7] : ""),
                    JsonUtil.escapeJson(row[8] != null ? row[8] : "EMPLOYEE")
                ));
            }
            sb.append("]");
            HttpUtil.sendJson(ex, 200, sb.toString());
        } catch (Exception e) {
            System.err.println("[employees] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }
}
