package com.springcli.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcli.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class SpringInitializrClient {

    private static final String BASE_URL = "https://start.spring.io";
    private static final String METADATA_ENDPOINT = "/metadata/client";
    private static final String STARTER_ENDPOINT = "/starter.zip";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public SpringInitializrClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.objectMapper = objectMapper;
    }

    public SpringMetadata fetchMetadata() {
        try {
            log.info("Fetching metadata from Spring Initializr...");

            String response = webClient.get()
                    .uri(METADATA_ENDPOINT)
                    .header("Accept", "application/vnd.initializr.v2.2+json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseMetadata(response);

        } catch (Exception e) {
            log.error("Failed to fetch metadata from Spring Initializr", e);
            throw new RuntimeException("Failed to connect to Spring Initializr: " + e.getMessage(), e);
        }
    }

    public void downloadProject(ProjectConfig config, Path outputPath) {
        try {
            log.info("Downloading project from Spring Initializr...");

            Map<String, String> params = buildQueryParams(config);

            webClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path(STARTER_ENDPOINT);
                        params.forEach(uriBuilder::queryParam);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .transform(flux -> DataBufferUtils.write(
                            flux,
                            outputPath,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING
                    ))
                    .blockLast();

            log.info("Project downloaded successfully to: {}", outputPath);

        } catch (Exception e) {
            log.error("Failed to download project", e);
            throw new RuntimeException("Failed to download project: " + e.getMessage(), e);
        }
    }

    private Map<String, String> buildQueryParams(ProjectConfig config) {
        Map<String, String> params = new LinkedHashMap<>();

        params.put("type", config.buildTool());
        params.put("language", "java");
        params.put("bootVersion", config.springBootVersion());
        params.put("groupId", config.groupId());
        params.put("artifactId", config.artifactId());
        params.put("name", config.name());
        params.put("description", config.description());
        params.put("packageName", config.packageName());
        params.put("packaging", config.packaging());
        params.put("javaVersion", config.javaVersion());
        params.put("baseDir", config.artifactId());

        if (!config.dependencies().isEmpty()) {
            String deps = String.join(",", config.dependencies());
            params.put("dependencies", deps);
        }

        return params;
    }

    private SpringMetadata parseMetadata(String jsonResponse) throws IOException {
        JsonNode root = objectMapper.readTree(jsonResponse);

        String defaultBootVersion = extractDefaultValue(root, "bootVersion");
        List<String> bootVersions = extractValues(root, "bootVersion");
        List<String> javaVersions = extractValues(root, "javaVersion");
        List<String> packagingTypes = extractValues(root, "packaging");

        List<BuildToolOption> buildTools = parseBuildTools(root);
        String defaultBuildTool = extractDefaultValue(root, "type");

        List<String> languages = extractValues(root, "language");
        String defaultLanguage = extractDefaultValue(root, "language");

        Map<String, DependencyGroup> dependencyGroups = parseDependencies(root);

        return new SpringMetadata(
                defaultBootVersion,
                bootVersions,
                javaVersions,
                packagingTypes,
                buildTools,
                defaultBuildTool,
                languages,
                defaultLanguage,
                dependencyGroups,
                System.currentTimeMillis()
        );
    }

    private List<BuildToolOption> parseBuildTools(JsonNode root) {
        List<BuildToolOption> buildTools = new ArrayList<>();
        JsonNode typeNode = root.path("type").path("values");

        for (JsonNode toolNode : typeNode) {
            String id = toolNode.path("id").asText();
            String name = toolNode.path("name").asText();
            String description = toolNode.path("description").asText("");
            if (id.contains("maven") || id.contains("gradle")) {
                buildTools.add(new BuildToolOption(id, name, description));
            }
        }
        return buildTools;
    }

    private String extractDefaultValue(JsonNode root, String fieldName) {
        return root.path(fieldName).path("default").asText();
    }

    private List<String> extractValues(JsonNode root, String fieldName) {
        JsonNode values = root.path(fieldName).path("values");
        return StreamSupport.stream(values.spliterator(), false)
                .map(node -> node.path("id").asText())
                .collect(Collectors.toList());
    }

    private Map<String, DependencyGroup> parseDependencies(JsonNode root) {
        Map<String, DependencyGroup> groups = new LinkedHashMap<>();

        JsonNode dependenciesNode = root.path("dependencies").path("values");

        for (JsonNode groupNode : dependenciesNode) {
            String groupName = groupNode.path("name").asText();
            List<Dependency> dependencies = new ArrayList<>();

            JsonNode values = groupNode.path("values");
            for (JsonNode depNode : values) {
                String id = depNode.path("id").asText();
                String name = depNode.path("name").asText();
                String description = depNode.path("description").asText("");

                dependencies.add(new Dependency(id, name, description));
            }

            groups.put(groupName, new DependencyGroup(groupName, dependencies));
        }

        return groups;
    }
}