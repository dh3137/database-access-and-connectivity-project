package com.cardealership.controller;

import com.cardealership.AppContext;
import com.cardealership.model.User;
import com.cardealership.service.AuthService;
import com.cardealership.util.HttpUtil;
import com.cardealership.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;

/**
 * Handles sign-in, sign-out, session inspection, and customer registration.
 */
public class AuthController {

    private final AppContext context;
    private final AuthService authService;

    public AuthController(AppContext context, AuthService authService) {
        this.context = context;
        this.authService = authService;
    }

    public void handleLogin(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equals(ex.getRequestMethod())) {
                HttpUtil.redirect(ex, "/login.html");
                return;
            }

            String body = HttpUtil.readBody(ex);
            System.out.println("[login] body: " + body);
            Map<String, String> params = HttpUtil.parseForm(body);
            User user = authService.authenticate(params.get("username"), params.get("password"));
            System.out.println("[login] user found: " + (user != null ? user.getUsername() : "null"));

            if (user != null) {
                String token = authService.createSession(user);
                System.out.println("[login] " + user.getUsername() + " logged in, role=" + user.getRole());
                authService.logGeneralAction(user, "LOGIN", "Session", String.valueOf(user.getId()), "Successful login");
                String dest = authService.isStaff(user) ? "/dashboard.html" : "/cars.html";
                authService.attachSessionCookie(ex, token);
                HttpUtil.redirect(ex, dest);
            } else {
                HttpUtil.redirect(ex, "/login.html?error=1");
            }
        } catch (Exception e) {
            System.err.println("[login] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.redirect(ex, "/login.html?error=1");
        }
    }

    public void handleLogout(HttpExchange ex) throws IOException {
        User user = authService.getSessionUser(ex);
        if (user != null) {
            authService.logGeneralAction(user, "LOGOUT", "Session", String.valueOf(user.getId()), "User logged out");
        }
        authService.clearSession(ex);
        HttpUtil.redirect(ex, "/login.html");
    }

    public void handleMe(HttpExchange ex) throws IOException {
        try {
            User user = authService.getSessionUser(ex);
            System.out.println("[me] cookie session user: " + (user != null ? user.getUsername() : "null"));
            if (user == null) {
                HttpUtil.sendJson(ex, 401, "{\"error\":\"Unauthorized\"}");
                return;
            }

            String json = authService.userToJson(user);
            if ("CUSTOMER".equals(user.getRole()) && user.getCustomerId() > 0) {
                String[] cust = context.customerDatabase.getCustomerById(user.getCustomerId());
                if (cust != null) {
                    json = json.substring(0, json.length() - 1)
                        + ",\"firstName\":\"" + JsonUtil.escapeJson(cust[1] != null ? cust[1] : "") + "\""
                        + ",\"lastName\":\"" + JsonUtil.escapeJson(cust[2] != null ? cust[2] : "") + "\""
                        + ",\"email\":\"" + JsonUtil.escapeJson(cust[3] != null ? cust[3] : "") + "\""
                        + "}";
                }
            }
            HttpUtil.sendJson(ex, 200, json);
        } catch (Exception e) {
            System.err.println("[me] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }

    public void handleRegister(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equals(ex.getRequestMethod())) {
                HttpUtil.sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = HttpUtil.readBody(ex);
            String username = JsonUtil.jsonString(body, "username").trim();
            String password = JsonUtil.jsonString(body, "password").trim();
            String firstName = JsonUtil.jsonString(body, "firstName").trim();
            String lastName = JsonUtil.jsonString(body, "lastName").trim();
            String email = JsonUtil.jsonString(body, "email").trim();
            String phone = JsonUtil.jsonString(body, "phone").trim();

            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"All fields are required\"}");
                return;
            }
            if (username.length() < 3 || username.length() > 50) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"Username must be between 3 and 50 characters\"}");
                return;
            }
            if (password.length() < 8) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"Password must be at least 8 characters long\"}");
                return;
            }
            if (!email.contains("@")) {
                HttpUtil.sendJson(ex, 400, "{\"error\":\"Invalid email address\"}");
                return;
            }
            if (context.userDatabase.getUserByUsername(username) != null) {
                HttpUtil.sendJson(ex, 409, "{\"error\":\"Username already taken\"}");
                return;
            }
            if (context.customerDatabase.emailExists(email)) {
                HttpUtil.sendJson(ex, 409, "{\"error\":\"An account with this email already exists\"}");
                return;
            }

            User created = authService.registerCustomer(username, password, firstName, lastName, email, phone);
            if (created != null) {
                authService.attachSessionCookie(ex, authService.createSession(created));
                authService.logGeneralAction(
                    created,
                    "CUSTOMER_REGISTERED",
                    "Customer",
                    String.valueOf(created.getCustomerId()),
                    "Customer account created for " + email
                );
            } else {
                HttpUtil.sendJson(ex, 500, "{\"error\":\"Could not create user account\"}");
                return;
            }
            HttpUtil.sendJson(ex, 201, "{\"ok\":true}");
        } catch (Exception e) {
            System.err.println("[register] ERROR: " + e.getMessage());
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, "{\"error\":\"Server error\"}");
        }
    }
}
