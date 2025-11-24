package com.springcli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcli.model.SpringMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Service
public class CacheService {

    private static final String CACHE_DIR = System.getProperty("user.home") + "/.spring-cli";
    private static final String CACHE_FILE = "metadata-cache.json";
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000;

    private final ObjectMapper objectMapper;
    private final Path cacheFilePath;

    public CacheService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.cacheFilePath = Paths.get(CACHE_DIR, CACHE_FILE);
        ensureCacheDirectoryExists();
    }

    public Optional<SpringMetadata> getCachedMetadata() {
        if (!Files.exists(cacheFilePath)) {
            log.debug("Cache file does not exist");
            return Optional.empty();
        }

        try {
            SpringMetadata metadata = objectMapper.readValue(cacheFilePath.toFile(), SpringMetadata.class);

            if (metadata.isExpired(CACHE_EXPIRY_MS)) {
                log.info("Cache expired, will fetch fresh metadata");
                return Optional.empty();
            }

            log.info("Using cached metadata");
            return Optional.of(metadata);

        } catch (IOException e) {
            log.warn("Failed to read cache file, will fetch fresh metadata", e);
            return Optional.empty();
        }
    }

    public void cacheMetadata(SpringMetadata metadata) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(cacheFilePath.toFile(), metadata);
            log.info("Metadata cached successfully");
        } catch (IOException e) {
            log.warn("Failed to cache metadata", e);
        }
    }

    public void clearCache() {
        try {
            if (Files.exists(cacheFilePath)) {
                Files.delete(cacheFilePath);
                log.info("Cache cleared successfully");
            }
        } catch (IOException e) {
            log.error("Failed to clear cache", e);
        }
    }

    private void ensureCacheDirectoryExists() {
        try {
            Path cacheDir = Paths.get(CACHE_DIR);
            if (!Files.exists(cacheDir)) {
                Files.createDirectories(cacheDir);
            }
        } catch (IOException e) {
            log.error("Failed to create cache directory", e);
        }
    }
}
