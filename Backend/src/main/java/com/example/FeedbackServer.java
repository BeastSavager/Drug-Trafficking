package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import com.sun.net.httpserver.*;

public class FeedbackServer {
    private static final ObjectMapper M = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String supabaseUrl = System.getenv("SUPABASE_URL"); // e.g. https://xxxx.supabase.co
        String supabaseServiceKey = System.getenv("SUPABASE_SERVICE_ROLE_KEY");
        if (supabaseUrl == null || supabaseServiceKey == null) {
            System.err.println("Set SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY env vars.");
            System.exit(1);
        }

        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(3000), 0);

        // Handler for feedback endpoint
        server.createContext("/api/feedback", exchange -> {
            try {
                // handle CORS preflight
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    addCorsHeaders(exchange);
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                addCorsHeaders(exchange);

                // read request body
                String body;
                try (InputStream is = exchange.getRequestBody()) {
                    body = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));
                }

                JsonNode incoming = M.readTree(body);
                String name = incoming.path("name").asText("");
                String email = incoming.path("email").asText(null);
                String feedback = incoming.path("feedback").asText("");

                if (name.isBlank() || feedback.isBlank()) {
                    byte[] resp = "name and feedback are required".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(400, resp.length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
                    return;
                }

                // prepare payload for Supabase (PostgREST accepts array for insert)
                ObjectNode row = M.createObjectNode();
                row.put("name", name);
                if (email != null && !email.isBlank()) row.put("email", email);
                row.put("feedback", feedback);
                String payload = M.writeValueAsString(new Object[] { row });

                // call Supabase REST endpoint
                String restUrl = supabaseUrl.replaceAll("/$", "") + "/rest/v1/feedback";
                HttpClient http = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(restUrl + "?return=representation"))
                    .header("Content-Type", "application/json")
                    .header("apikey", supabaseServiceKey)
                    .header("Authorization", "Bearer " + supabaseServiceKey)
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

                HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
                int status = res.statusCode();
                String respBody = res.body();

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, respBody.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(respBody.getBytes(StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                e.printStackTrace();
                String err = "{\"error\":\"internal server error\"}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, err.getBytes(StandardCharsets.UTF_8).length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err.getBytes(StandardCharsets.UTF_8)); }
            }
        });

        server.start();
        System.out.println("Listening on http://localhost:3000");
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // use a specific origin in prod
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
