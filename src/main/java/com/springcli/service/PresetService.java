package com.springcli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcli.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class PresetService {

    private static final String PRESET_DIR = System.getProperty("user.home") + "/.spring-cli/presets";
    private final ObjectMapper objectMapper;
    private final Path presetDirPath;

    public PresetService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.presetDirPath = Paths.get(PRESET_DIR);
        ensurePresetDirectoryExists();
    }

    public List<Preset> getAllPresets() {
        List<Preset> allPresets = new ArrayList<>(getBuiltInPresets());
        allPresets.addAll(getUserPresets());
        return allPresets;
    }

    public List<Preset> getBuiltInPresets() {
        return List.of(
                new Preset("REST-API", "Clean Architecture REST API", Architecture.CLEAN, "21",
                        Set.of("web", "data-jpa", "h2", "validation", "lombok", "devtools"),
                        new ProjectFeatures(true, true, true, true, true, false, false, false, true), true),

                new Preset("GraphQL-API", "GraphQL API with Spring for GraphQL", Architecture.CLEAN, "21",
                        Set.of("web", "graphql", "data-jpa", "h2", "validation", "lombok", "devtools"),
                        new ProjectFeatures(true, false, true, true, true, false, false, false, true), true),

                new Preset("Microservice", "Hexagonal architecture microservice", Architecture.HEXAGONAL, "21",
                        Set.of("web", "data-jpa", "postgresql", "cloud-eureka", "cloud-config-client", "actuator", "lombok"),
                        new ProjectFeatures(true, true, true, true, true, true, true, true, true), true),

                new Preset("Monolith", "Traditional MVC monolith", Architecture.MVC, "21",
                        Set.of("web", "thymeleaf", "data-jpa", "mysql", "security", "validation", "lombok"),
                        new ProjectFeatures(false, false, false, true, false, true, false, false, false), true),

                new Preset("Minimal", "Minimal Spring Boot app", Architecture.MVC, "21",
                        Set.of("web", "lombok", "devtools"),
                        ProjectFeatures.defaults(), true)
        );
    }

    public List<Preset> getUserPresets() {
        try {
            if (!Files.exists(presetDirPath)) return List.of();
            try (Stream<Path> paths = Files.list(presetDirPath)) {
                return paths.filter(path -> path.toString().endsWith(".json"))
                        .map(this::loadPresetFromFile)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("Failed to load user presets", e);
            return List.of();
        }
    }

    public void savePreset(Preset preset) {
        try {
            // Cria uma versão custom (não built-in) para salvar
            Preset customPreset = new Preset(
                    preset.name(),
                    preset.description(),
                    preset.architecture(),
                    preset.javaVersion(),
                    preset.dependencies(),
                    preset.features(),
                    false // Sempre salva como custom
            );

            String fileName = sanitizeFileName(preset.name()) + ".json";
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(presetDirPath.resolve(fileName).toFile(), customPreset);
            log.info("Preset '{}' saved successfully", preset.name());
        } catch (IOException e) {
            log.error("Failed to save preset", e);
            throw new RuntimeException("Failed to save preset: " + e.getMessage(), e);
        }
    }

    public Optional<Preset> getPresetByName(String name) {
        return getAllPresets().stream()
                .filter(p -> p.name().equalsIgnoreCase(name))
                .findFirst();
    }

    private String sanitizeFileName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9-_]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    public void deletePreset(String presetName) {
        try {
            String fileName = sanitizeFileName(presetName) + ".json";
            Path presetFile = presetDirPath.resolve(fileName);
            if (Files.exists(presetFile)) {
                Files.delete(presetFile);
                log.info("Preset '{}' deleted successfully", presetName);
            } else {
                log.warn("Preset file not found: {}", fileName);
            }
        } catch (IOException e) {
            log.error("Failed to delete preset", e);
            throw new RuntimeException("Failed to delete preset: " + e.getMessage(), e);
        }
    }

    private Optional<Preset> loadPresetFromFile(Path path) {
        try {
            return Optional.of(objectMapper.readValue(path.toFile(), Preset.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private void ensurePresetDirectoryExists() {
        try {
            if (!Files.exists(presetDirPath)) Files.createDirectories(presetDirPath);
        } catch (IOException e) {
            log.error("Failed to create preset directory", e);
        }
    }
}