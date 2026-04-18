package com.cardealership.service;

import com.cardealership.dao.UserDao;
import com.cardealership.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserService {

    private final UserDao userDao = new UserDao();

    public User authenticate(String username, String plainPassword) {
        if (username == null || username.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            return null;
        }

        User user = userDao.findByUsername(username);
        if (user == null) {
            return null;
        }

        String hash = sha256(plainPassword);
        if (hash.equals(user.getPasswordHash())) {
            return user;
        }

        return null;
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
