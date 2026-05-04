package com.cardealership;

import com.cardealership.controller.AuthController;
import com.cardealership.controller.CatalogController;
import com.cardealership.controller.ManagementController;
import com.cardealership.service.AuthService;
import com.cardealership.web.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Application bootstrap only.
 * All feature logic now lives in focused services, controllers, and helpers.
 */
public class Main {

    private static final String STATIC_DIR = "src/main/webapp";

    public static void main(String[] args) throws IOException {
        AppContext context = new AppContext(AppDatabaseLoader.load());

        try {
            boolean connected = context.database.connect();
            System.out.println("Connected: " + connected);
        } catch (DLException e) {
            System.err.println(e.getMessage());
            return;
        }

        AuthService authService = new AuthService(context);
        AuthController authController = new AuthController(context, authService);
        CatalogController catalogController = new CatalogController(context, authService);
        ManagementController managementController = new ManagementController(context, authService);
        StaticFileHandler staticFileHandler = new StaticFileHandler(STATIC_DIR);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/login", authController::handleLogin);
        server.createContext("/api/logout", authController::handleLogout);
        server.createContext("/api/me", authController::handleMe);
        server.createContext("/api/register", authController::handleRegister);

        server.createContext("/api/cars", catalogController::handleCars);
        server.createContext("/api/logs", catalogController::handleLogs);
        server.createContext("/api/carimage", catalogController::handleCarImage);
        server.createContext("/api/models", catalogController::handleModels);
        server.createContext("/api/reviews", catalogController::handleReviews);
        server.createContext("/api/maintenance", catalogController::handleMaintenance);

        server.createContext("/api/enquiry", managementController::handleEnquiry);
        server.createContext("/api/enquiries", managementController::handleEnquiries);
        server.createContext("/api/sales", managementController::handleSales);
        server.createContext("/api/customers", managementController::handleCustomers);
        server.createContext("/api/employees", managementController::handleEmployees);
        server.createContext("/", staticFileHandler::handle);

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("AutoPrime running → http://localhost:8080");
    }
}
