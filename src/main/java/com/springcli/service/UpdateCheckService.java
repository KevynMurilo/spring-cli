package com.springcli.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCheckService {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/YOUR_REPO/releases/latest";
    private static final String CURRENT_VERSION = "1.1.0";
    private final ObjectMapper objectMapper;

    public UpdateInfo checkForUpdates() {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API_URL))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());
                String latestVersion = json.get("tag_name").asText().replace("v", "");
                String releaseUrl = json.get("html_url").asText();

                if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
                    return new UpdateInfo(true, latestVersion, CURRENT_VERSION, releaseUrl);
                }
            }

            return new UpdateInfo(false, CURRENT_VERSION, CURRENT_VERSION, null);
        } catch (IOException | InterruptedException e) {
            log.debug("Could not check for updates: {}", e.getMessage());
            return new UpdateInfo(false, CURRENT_VERSION, CURRENT_VERSION, null);
        }
    }

    private boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        for (int i = 0; i < Math.min(latestParts.length, currentParts.length); i++) {
            int latestPart = Integer.parseInt(latestParts[i]);
            int currentPart = Integer.parseInt(currentParts[i]);

            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }

        return latestParts.length > currentParts.length;
    }

    public record UpdateInfo(
        boolean updateAvailable,
        String latestVersion,
        String currentVersion,
        String releaseUrl
    ) {}
}
