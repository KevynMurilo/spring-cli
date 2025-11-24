package com.springcli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcli.model.UserConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class ConfigService {

    private static final String CONFIG_FILE = System.getProperty("user.home") + "/.springclirc.json";

    private final ObjectMapper objectMapper;
    private final Path configFilePath;

    public ConfigService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.configFilePath = Paths.get(CONFIG_FILE);
    }

    public UserConfig loadConfig() {
        if (!Files.exists(configFilePath)) {
            log.info("Config file not found, using defaults");
            return UserConfig.defaults();
        }

        try {
            return objectMapper.readValue(configFilePath.toFile(), UserConfig.class);
        } catch (IOException e) {
            log.warn("Failed to read config file, using defaults", e);
            return UserConfig.defaults();
        }
    }

    public void saveConfig(UserConfig config) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(configFilePath.toFile(), config);
            log.info("Configuration saved successfully");
        } catch (IOException e) {
            log.error("Failed to save configuration", e);
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    public void resetConfig() {
        try {
            if (Files.exists(configFilePath)) {
                Files.delete(configFilePath);
            }
            saveConfig(UserConfig.defaults());
            log.info("Configuration reset to defaults");
        } catch (IOException e) {
            log.error("Failed to reset configuration", e);
        }
    }
}
