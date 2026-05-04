package com.cardealership.web;

import com.cardealership.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves the static frontend files from src/main/webapp while blocking simple
 * path traversal attempts.
 */
public class StaticFileHandler {

    private final String staticDir;

    public StaticFileHandler(String staticDir) {
        this.staticDir = staticDir;
    }

    public void handle(HttpExchange ex) throws IOException {
        String uriPath = ex.getRequestURI().getPath();
        if ("/".equals(uriPath)) {
            uriPath = "/index.html";
        }

        Path file = Paths.get(staticDir + uriPath).normalize();
        Path base = Paths.get(staticDir).normalize();
        if (!file.startsWith(base)) {
            HttpUtil.sendPlain(ex, 403, "Forbidden");
            return;
        }

        if (Files.exists(file) && !Files.isDirectory(file)) {
            byte[] bytes = Files.readAllBytes(file);
            ex.getResponseHeaders().add("Content-Type", HttpUtil.contentType(uriPath));
            ex.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(bytes);
            }
        } else {
            HttpUtil.sendPlain(ex, 404, "Not Found");
        }
    }
}
