package com.example.employeeapi.handlers;

import com.example.employeeapi.dao.UserDAO;
import com.example.employeeapi.models.User;
import com.example.employeeapi.util.JsonUtil;
import com.example.employeeapi.util.JwtUtil;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * LoginHandler handles user authentication.
 * POST /api/login - Validates credentials and returns JWT token
 */
public class LoginHandler implements HttpHandler {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method)) {
                handleLogin(exchange);
            } else {
                sendResponse(exchange, 405, JsonUtil.errorResponse("Method not allowed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, JsonUtil.errorResponse("Internal server error: " + e.getMessage()));
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            System.out.printf("[%s] %s - Status: %d - Duration: %dms%n",
                    method, path, exchange.getResponseCode(), duration);
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            // Read request body
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Parse JSON
            JsonObject json = JsonUtil.fromJson(body, JsonObject.class);
            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();

            // Validate credentials
            User user = userDAO.findByUsername(username);
            if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
                sendResponse(exchange, 401, JsonUtil.errorResponse("Invalid credentials"));
                return;
            }

            // Generate JWT token
            String token = JwtUtil.generateToken(username);

            // Create response
            JsonObject response = new JsonObject();
            response.addProperty("token", token);
            response.addProperty("expiresIn", 3600);

            sendResponse(exchange, 200, JsonUtil.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 400, JsonUtil.errorResponse("Invalid request format"));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
