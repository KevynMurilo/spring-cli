package com.springcli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcli.model.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebServerService {

    private final MetadataService metadataService;
    private final PresetService presetService;
    private final ProjectGeneratorService projectGeneratorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpServer httpServer;
    private int actualPort;
    private boolean isRunning = false;

    public void startAndOpenBrowser() {
        if (isRunning) {
            log.info("Web server is already running on port {}", actualPort);
            return;
        }

        try {
            actualPort = findAvailablePort();
            log.info("Starting web server on port {}...", actualPort);

            httpServer = HttpServer.create(new InetSocketAddress(actualPort), 0);

            httpServer.createContext("/", this::handleStaticFiles);
            httpServer.createContext("/api/metadata", this::handleMetadata);
            httpServer.createContext("/api/architectures", this::handleArchitectures);
            httpServer.createContext("/api/presets", this::handlePresets);
            httpServer.createContext("/api/options", this::handleOptions);
            httpServer.createContext("/api/features", this::handleFeatures);
            httpServer.createContext("/api/generate", this::handleGenerate);

            httpServer.setExecutor(null);
            httpServer.start();

            isRunning = true;
            log.info("Web server successfully started on port {}", actualPort);

            openBrowser(actualPort);

            log.info("Web GUI is now accessible at http://localhost:{}", actualPort);
            log.info("Press Enter to return to CLI...");

        } catch (Exception e) {
            log.error("Failed to start web server", e);
            isRunning = false;
            stop();
        }
    }

    private int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            log.warn("Could not find available port, using default 8080");
            return 8080;
        }
    }

    private void openBrowser(int port) {
        String url = "http://localhost:" + port;

        try {
            String os = System.getProperty("os.name").toLowerCase();
            Runtime runtime = Runtime.getRuntime();

            if (os.contains("win")) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                log.info("Opening browser at {}", url);
            } else if (os.contains("mac")) {
                runtime.exec("open " + url);
                log.info("Opening browser at {}", url);
            } else if (os.contains("nix") || os.contains("nux")) {
                runtime.exec("xdg-open " + url);
                log.info("Opening browser at {}", url);
            } else {
                log.warn("Could not detect OS to open browser automatically. Please open: {}", url);
            }
        } catch (IOException e) {
            log.warn("Could not open browser automatically. Please open: {}", url);
        }
    }

    public void stop() {
        if (isRunning && httpServer != null) {
            log.info("Stopping web server...");
            httpServer.stop(0);
            httpServer = null;
            isRunning = false;
            log.info("Web server stopped");
        }
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.equals("/index.html")) {
            path = "/static/index.html";
        } else if (!path.startsWith("/static/")) {
            path = "/static" + path;
        }

        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                byte[] bytes = resource.getInputStream().readAllBytes();
                String contentType = getContentType(path);

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, bytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            log.error("Error serving static file: " + path, e);
            String response = "500 Internal Server Error";
            exchange.sendResponseHeaders(500, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private void handleMetadata(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            SpringMetadata metadata = metadataService.getMetadata();
            String json = objectMapper.writeValueAsString(metadata);
            sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            log.error("Error fetching metadata", e);
            sendJsonResponse(exchange, 500, "{\"error\": \"Failed to fetch metadata\"}");
        }
    }

    private void handleArchitectures(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        List<Map<String, String>> architectures = Arrays.stream(Architecture.values())
                .map(arch -> Map.of("name", arch.name(), "displayName", arch.getDisplayName()))
                .collect(Collectors.toList());

        String json = objectMapper.writeValueAsString(architectures);
        sendJsonResponse(exchange, 200, json);
    }

    private void handlePresets(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        List<Preset> presets = presetService.getAllPresets();
        String json = objectMapper.writeValueAsString(presets);
        sendJsonResponse(exchange, 200, json);
    }

    private void handleOptions(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            SpringMetadata metadata = metadataService.getMetadata();
            List<String> buildTools = metadata.buildTools().stream()
                    .map(bt -> bt.id())
                    .toList();

            Map<String, Object> options = Map.of(
                    "springBootVersions", metadata.springBootVersions(),
                    "javaVersions", metadata.javaVersions(),
                    "buildTools", buildTools
            );

            String json = objectMapper.writeValueAsString(options);
            sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            log.error("Error fetching options", e);
            sendJsonResponse(exchange, 500, "{\"error\": \"Failed to fetch options\"}");
        }
    }

    private void handleFeatures(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        List<Map<String, String>> features = Arrays.asList(
                Map.of("id", "enableJwt", "label", "JWT Authentication", "description", "Add JWT token-based authentication", "icon", "fa-shield-alt"),
                Map.of("id", "enableSwagger", "label", "Swagger/OpenAPI", "description", "Generate interactive API documentation", "icon", "fa-file-code"),
                Map.of("id", "enableCors", "label", "CORS Configuration", "description", "Enable Cross-Origin Resource Sharing", "icon", "fa-globe"),
                Map.of("id", "enableExceptionHandler", "label", "Exception Handler", "description", "Global exception handling mechanism", "icon", "fa-exclamation-triangle"),
                Map.of("id", "enableMapStruct", "label", "MapStruct", "description", "Object mapping library for DTOs", "icon", "fa-map-signs"),
                Map.of("id", "enableDocker", "label", "Docker Support", "description", "Dockerfile and docker-compose", "icon", "fa-docker"),
                Map.of("id", "enableKubernetes", "label", "Kubernetes", "description", "K8s deployment manifests", "icon", "fa-dharmachakra"),
                Map.of("id", "enableCiCd", "label", "CI/CD Pipeline", "description", "GitHub Actions workflow", "icon", "fa-sync"),
                Map.of("id", "enableAudit", "label", "Audit Logging", "description", "Track entity changes", "icon", "fa-history")
        );

        String json = objectMapper.writeValueAsString(features);
        sendJsonResponse(exchange, 200, json);
    }

    private void handleGenerate(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> request = objectMapper.readValue(requestBody, Map.class);

            Map<String, Object> featuresMap = (Map<String, Object>) request.get("features");
            ProjectFeatures features = new ProjectFeatures(
                    (Boolean) featuresMap.getOrDefault("enableJwt", false),
                    (Boolean) featuresMap.getOrDefault("enableSwagger", false),
                    (Boolean) featuresMap.getOrDefault("enableCors", false),
                    (Boolean) featuresMap.getOrDefault("enableExceptionHandler", false),
                    (Boolean) featuresMap.getOrDefault("enableMapStruct", false),
                    (Boolean) featuresMap.getOrDefault("enableDocker", false),
                    (Boolean) featuresMap.getOrDefault("enableKubernetes", false),
                    (Boolean) featuresMap.getOrDefault("enableCiCd", false),
                    (Boolean) featuresMap.getOrDefault("enableAudit", false)
            );

            ProjectConfig config = ProjectConfig.builder()
                    .groupId((String) request.get("groupId"))
                    .artifactId((String) request.get("artifactId"))
                    .name((String) request.get("projectName"))
                    .description((String) request.get("description"))
                    .packageName((String) request.get("packageName"))
                    .javaVersion((String) request.get("javaVersion"))
                    .buildTool((String) request.get("buildTool"))
                    .packaging("jar")
                    .architecture(Architecture.valueOf((String) request.get("architecture")))
                    .springBootVersion((String) request.get("springBootVersion"))
                    .dependencies(new HashSet<>((List<String>) request.get("dependencies")))
                    .features(features)
                    .outputDirectory((String) request.get("outputPath"))
                    .build();

            projectGeneratorService.generateProject(config);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Project generated successfully at: " + request.get("outputPath"),
                    "projectPath", request.get("outputPath")
            );

            String json = objectMapper.writeValueAsString(response);
            sendJsonResponse(exchange, 200, json);

        } catch (Exception e) {
            log.error("Failed to generate project", e);
            Map<String, Object> response = Map.of(
                    "success", false,
                    "message", "Failed to generate project: " + e.getMessage(),
                    "projectPath", (Object) null
            );
            String json = objectMapper.writeValueAsString(response);
            sendJsonResponse(exchange, 500, json);
        }
    }

    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getServerPort() {
        return actualPort;
    }
}
