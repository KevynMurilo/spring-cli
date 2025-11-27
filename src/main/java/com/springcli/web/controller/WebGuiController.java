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
                .map(arch -> new ArchitectureInfo(arch.name(), arch.getDisplayName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(architectures);
    }

    @GetMapping("/presets")
    public ResponseEntity<List<Preset>> getPresets() {
        return ResponseEntity.ok(presetService.getAllPresets());
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
                    buildTools
            ));
        } catch (Exception e) {
            log.error("Failed to fetch project options", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/features")
    public ResponseEntity<List<FeatureInfo>> getFeatures() {
        List<FeatureInfo> features = Arrays.asList(
                new FeatureInfo("enableJwt", "JWT Authentication", "Add JWT token-based authentication", "fa-shield-alt"),
                new FeatureInfo("enableSwagger", "Swagger/OpenAPI", "Generate interactive API documentation", "fa-file-code"),
                new FeatureInfo("enableCors", "CORS Configuration", "Enable Cross-Origin Resource Sharing", "fa-globe"),
                new FeatureInfo("enableExceptionHandler", "Exception Handler", "Global exception handling mechanism", "fa-exclamation-triangle"),
                new FeatureInfo("enableMapStruct", "MapStruct", "Object mapping library for DTOs", "fa-map-signs"),
                new FeatureInfo("enableDocker", "Docker Support", "Dockerfile and docker-compose", "fa-docker"),
                new FeatureInfo("enableKubernetes", "Kubernetes", "K8s deployment manifests", "fa-dharmachakra"),
                new FeatureInfo("enableCiCd", "CI/CD Pipeline", "GitHub Actions workflow", "fa-sync"),
                new FeatureInfo("enableAudit", "Audit Logging", "Track entity changes", "fa-history")
        );
        return ResponseEntity.ok(features);
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
                    .buildTool(request.buildTool())
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