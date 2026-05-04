package com.cardealership.service;

import com.cardealership.AppContext;
import com.cardealership.DLException;
import com.cardealership.model.User;
import com.cardealership.util.HttpUtil;
import com.cardealership.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.sql.Connection;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns authentication, session storage, and the shared account-role checks
 * used across controllers.
 */
public class AuthService {

    private static final String SESSION_COOKIE = "session";
    private static final String SESSION_COOKIE_VALUE =
        SESSION_COOKIE + "=%s; Path=/; HttpOnly; Max-Age=604800; SameSite=Strict";

    private final AppContext context;
    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public AuthService(AppContext context) {
        this.context = context;
    }

    public User authenticate(String username, String password) throws DLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }

        return context.userDatabase.authenticate(username, sha256(password));
    }

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public void attachSessionCookie(HttpExchange ex, String token) {
        ex.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE_VALUE.formatted(token));
    }

    public void clearSession(HttpExchange ex) {
        String token = HttpUtil.getCookie(ex, SESSION_COOKIE);
        if (token != null) {
            sessions.remove(token);
        }
        ex.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE + "=; Path=/; Max-Age=0");
    }

    public User getSessionUser(HttpExchange ex) {
        String token = HttpUtil.getCookie(ex, SESSION_COOKIE);
        return token != null ? sessions.get(token) : null;
    }

    public boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }

    public boolean isStaff(User user) {
        return user != null && ("ADMIN".equals(user.getRole()) || "EMPLOYEE".equals(user.getRole()));
    }

    public boolean canManageCars(User user) {
        return isAdmin(user);
    }

    public boolean canAccessStaffFeatures(User user) {
        return isStaff(user);
    }

    public String userToJson(User user) {
        return String.format(
            "{\"id\":%d,\"username\":\"%s\",\"role\":\"%s\",\"empId\":%d,\"customerId\":%d,\"canManageCars\":%s}",
            user.getId(),
            JsonUtil.escapeJson(user.getUsername()),
            JsonUtil.escapeJson(user.getRole()),
            user.getEmpId(),
            user.getCustomerId(),
            canManageCars(user) ? "true" : "false"
        );
    }

    public String hashPassword(String input) {
        return sha256(input);
    }

    public User registerCustomer(String username, String password, String firstName, String lastName, String email, String phone)
        throws DLException {
        try (Connection connection = context.database.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                int customerId = context.customerDatabase.createCustomer(connection, firstName, lastName, email, phone);
                if (customerId <= 0) {
                    connection.rollback();
                    return null;
                }

                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(hashPassword(password));
                newUser.setRole("CUSTOMER");
                newUser.setCustomerId(customerId);

                if (!context.userDatabase.saveUser(connection, newUser)) {
                    connection.rollback();
                    return null;
                }

                connection.commit();
            } catch (DLException | SQLException e) {
                rollbackQuietly(connection);
                if (e instanceof DLException dlException) {
                    throw dlException;
                }
                throw new DLException((SQLException) e, "Operation=registerCustomer", "DatabaseType=MySQL");
            } finally {
                restoreAutoCommit(connection, originalAutoCommit);
            }
        } catch (DLException e) {
            throw e;
        } catch (SQLException e) {
            throw new DLException(e, "Operation=registerCustomer", "DatabaseType=MySQL");
        }

        return authenticate(username, password);
    }

    public void logGeneralAction(User user, String actionType, String objectType, String objectId, String details) {
        try {
            int userId = user != null ? user.getId() : 0;
            int empId = user != null ? user.getEmpId() : 0;
            context.actionLogDatabase.saveGeneralAction(userId, empId, actionType, objectType, objectId, details);
        } catch (DLException e) {
            System.err.println("[action_log] Failed to write log: " + e.getMessage());
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            System.err.println("[auth] Rollback failed: " + rollbackError.getMessage());
        }
    }

    private void restoreAutoCommit(Connection connection, boolean originalAutoCommit) throws DLException {
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            throw new DLException(e, "Operation=restoreAutoCommit", "DatabaseType=MySQL");
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
