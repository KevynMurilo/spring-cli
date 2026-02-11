package com.springcli.web.controller;

import com.springcli.model.*;
import com.springcli.service.*;
import com.springcli.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WebGuiController {

    private final MetadataService metadataService;
    private final PresetService presetService;
    private final ProjectGeneratorService projectGeneratorService;
    private final ConfigService configService;
    private final CacheService cacheService;

    @GetMapping("/metadata")
    public ResponseEntity<SpringMetadata> getMetadata() {
        try {
            return ResponseEntity.ok(metadataService.getMetadata());
        } catch (Exception e) {
            log.error("Failed to fetch metadata", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/architectures")
    public ResponseEntity<List<ArchitectureInfo>> getArchitectures() {
        List<ArchitectureInfo> architectures = Arrays.stream(Architecture.values())
                .map(arch -> new ArchitectureInfo(
                        arch.name(),
                        arch.getDisplayName(),
                        arch.getDescription(),
                        arch.getLayerMappings()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(architectures);
    }

    @GetMapping("/presets")
    public ResponseEntity<List<Preset>> getPresets() {
        return ResponseEntity.ok(presetService.getAllPresets());
    }

    @PostMapping("/presets")
    public ResponseEntity<Map<String, String>> createPreset(@RequestBody CreatePresetRequest request) {
        try {
            Preset preset = new Preset(
                    request.name(),
                    request.description(),
                    Architecture.valueOf(request.architecture()),
                    request.javaVersion(),
                    request.dependencies(),
                    request.features(),
                    false
            );
            presetService.savePreset(preset);
            return ResponseEntity.ok(Map.of("message", "Preset '" + request.name() + "' saved successfully"));
        } catch (Exception e) {
            log.error("Failed to save preset", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to save preset: " + e.getMessage()));
        }
    }

    @DeleteMapping("/presets/{name}")
    public ResponseEntity<Map<String, String>> deletePreset(@PathVariable String name) {
        try {
            presetService.deletePreset(name);
            return ResponseEntity.ok(Map.of("message", "Preset '" + name + "' deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete preset", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to delete preset: " + e.getMessage()));
        }
    }

    @GetMapping("/options")
    public ResponseEntity<ProjectOptions> getProjectOptions() {
        try {
            SpringMetadata metadata = metadataService.getMetadata();

            List<String> buildTools = metadata.buildTools().stream()
                    .map(bt -> bt.id())
                    .toList();

            return ResponseEntity.ok(new ProjectOptions(
                    metadata.springBootVersions(),
                    metadata.javaVersions(),
                    metadata.languages(),
                    buildTools,
                    metadata.packagingTypes()
            ));
        } catch (Exception e) {
            log.error("Failed to fetch project options", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/features")
    public ResponseEntity<List<FeatureInfo>> getFeatures() {
        List<FeatureInfo> features = Arrays.asList(
                new FeatureInfo("enableJwt", "JWT Authentication", "Add JWT token-based authentication with login endpoint", "fa-shield-alt", "Security", List.of("security")),
                new FeatureInfo("enableSwagger", "Swagger/OpenAPI", "Generate interactive API documentation at /swagger-ui", "fa-file-code", "API", List.of()),
                new FeatureInfo("enableCors", "CORS Configuration", "Allow cross-origin requests from frontend apps", "fa-globe", "API", List.of()),
                new FeatureInfo("enableExceptionHandler", "Exception Handler", "Global exception handling with standardized error responses", "fa-exclamation-triangle", "API", List.of()),
                new FeatureInfo("enableMapStruct", "MapStruct", "Compile-time object mapping between entities and DTOs", "fa-map-signs", "Data", List.of("data-jpa")),
                new FeatureInfo("enableAudit", "Audit Logging", "Auto-track createdAt, updatedAt, createdBy on entities", "fa-history", "Data", List.of("data-jpa")),
                new FeatureInfo("enableDocker", "Docker Support", "Dockerfile + docker-compose with database services", "fa-docker", "DevOps", List.of()),
                new FeatureInfo("enableKubernetes", "Kubernetes", "K8s deployment, service, and configmap manifests", "fa-dharmachakra", "DevOps", List.of()),
                new FeatureInfo("enableCiCd", "CI/CD Pipeline", "GitHub Actions workflow for build, test, and deploy", "fa-sync", "DevOps", List.of())
        );
        return ResponseEntity.ok(features);
    }

    @GetMapping("/config")
    public ResponseEntity<UserConfigDto> getConfig() {
        UserConfig config = configService.loadConfig();
        return ResponseEntity.ok(new UserConfigDto(
                config.defaultGroupId(),
                config.defaultJavaVersion(),
                config.defaultPackaging(),
                config.defaultArchitecture().name(),
                config.defaultOutputDir(),
                config.autoOpenIde(),
                config.preferredIde(),
                config.useApplicationYml(),
                config.generateReadme(),
                config.generateGitignore()
        ));
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, String>> saveConfig(@RequestBody UserConfigDto dto) {
        try {
            UserConfig config = new UserConfig(
                    dto.defaultGroupId(),
                    dto.defaultJavaVersion(),
                    dto.defaultPackaging(),
                    Architecture.valueOf(dto.defaultArchitecture()),
                    dto.defaultOutputDir(),
                    dto.autoOpenIde(),
                    dto.preferredIde(),
                    dto.useApplicationYml(),
                    dto.generateReadme(),
                    dto.generateGitignore()
            );
            configService.saveConfig(config);
            return ResponseEntity.ok(Map.of("message", "Configuration saved successfully"));
        } catch (Exception e) {
            log.error("Failed to save config", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to save: " + e.getMessage()));
        }
    }

    @PostMapping("/config/reset")
    public ResponseEntity<Map<String, String>> resetConfig() {
        try {
            configService.resetConfig();
            return ResponseEntity.ok(Map.of("message", "Configuration reset to defaults"));
        } catch (Exception e) {
            log.error("Failed to reset config", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to reset: " + e.getMessage()));
        }
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, String>> clearCache() {
        try {
            cacheService.clearCache();
            return ResponseEntity.ok(Map.of("message", "Cache cleared successfully"));
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to clear cache: " + e.getMessage()));
        }
    }

    @PostMapping("/metadata/refresh")
    public ResponseEntity<Map<String, String>> refreshMetadata() {
        try {
            cacheService.clearCache();
            metadataService.getMetadata();
            return ResponseEntity.ok(Map.of("message", "Metadata refreshed successfully"));
        } catch (Exception e) {
            log.error("Failed to refresh metadata", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to refresh: " + e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generateProject(@RequestBody GenerationRequest request) {
        try {
            log.info("Generating project via Web API: {}", request.projectName());

            ProjectConfig config = ProjectConfig.builder()
                    .groupId(request.groupId())
                    .artifactId(request.artifactId())
                    .name(request.projectName())
                    .description(request.description())
                    .packageName(request.packageName())
                    .springBootVersion(request.springBootVersion())
                    .javaVersion(request.javaVersion())
                    .language(request.language())
                    .buildTool(request.buildTool())
                    .packaging(request.packaging())
                    .dependencies(request.dependencies())
                    .architecture(Architecture.valueOf(request.architecture()))
                    .features(request.features())
                    .outputDirectory(request.outputPath())
                    .build();

            projectGeneratorService.generateProject(config);

            return ResponseEntity.ok(new GenerationResponse(
                    true,
                    "Project generated successfully at: " + request.outputPath(),
                    request.outputPath()
            ));
        } catch (Exception e) {
            log.error("Failed to generate project", e);
            return ResponseEntity.ok(new GenerationResponse(
                    false,
                    "Failed to generate project: " + e.getMessage(),
                    null
            ));
        }
    }
}
