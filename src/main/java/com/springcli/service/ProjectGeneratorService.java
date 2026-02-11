package com.springcli.service;

import com.springcli.client.SpringInitializrClient;
import com.springcli.infra.filesystem.FileSystemService;
import com.springcli.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectGeneratorService {

    private final SpringInitializrClient initializrClient;
    private final TemplateService templateService;
    private final FileSystemService fileSystemService;
    private final PomManipulationService pomManipulationService;
    private final GradleManipulationService gradleManipulationService;

    public void generateProject(ProjectConfig config) {
        try {
            log.info("Starting project generation for: {}", config.artifactId());

            Path baseOutputDir = Paths.get(config.outputDirectory());
            Path tempZip = Files.createTempFile("spring-cli-", ".zip");

            initializrClient.downloadProject(config, tempZip);
            fileSystemService.extractZip(tempZip, baseOutputDir);

            Path projectRoot = resolveRealProjectRoot(baseOutputDir, config.artifactId());

            generateStructure(config, projectRoot);
            injectDependencies(config, projectRoot);
            generateConfigFiles(config, projectRoot);

            if (config.features().enableDocker()) {
                generateDockerFiles(config, projectRoot);
            }
            if (config.features().enableKubernetes()) {
                generateKubernetesFiles(config, projectRoot);
            }
            if (config.features().enableCiCd()) {
                generateCiCdFiles(config, projectRoot);
            }

            generateGitignore(config, projectRoot);
            Files.deleteIfExists(tempZip);

            log.info("Project generated successfully at: {}", projectRoot);

        } catch (Exception e) {
            log.error("Failed to generate project", e);
            throw new RuntimeException("Failed to generate project: " + e.getMessage(), e);
        }
    }

    private void generateStructure(ProjectConfig config, Path projectRoot) throws IOException {
        Path srcMainJava = findJavaSourceRoot(projectRoot);
        Path basePackagePath = srcMainJava.resolve(config.packageName().replace('.', '/'));

        TemplateContext context = buildTemplateContext(config);
        Architecture arch = config.architecture();

        for (ArchitectureBlueprint blueprint : arch.getBlueprints()) {
            String layerPath = arch.getPathForLayer(blueprint.layer());
            String relativePath = layerPath.replace("{feature}", "demo").replace('.', '/');

            String fileName = blueprint.filenameSuffix().startsWith(".")
                    ? context.entityName() + blueprint.filenameSuffix()
                    : context.entityName() + blueprint.filenameSuffix();

            generateClass(basePackagePath, relativePath, fileName, blueprint.template(), context);
        }

        generateFeatureFiles(config, basePackagePath, context);
    }

    private void generateClass(Path basePackagePath, String relativePath, String fileName, String templateName, TemplateContext context) throws IOException {
        Path directory = basePackagePath.resolve(relativePath);
        fileSystemService.createDirectories(directory);

        String currentPackage = context.packageName() + (relativePath.isEmpty() ? "" : "." + relativePath.replace("/", "."));

        Map<String, Object> fileProps = new HashMap<>(context.additionalProperties());
        fileProps.put("currentPackage", currentPackage);

        Map<String, String> packageMap = context.architecture().getLayerMappings().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> resolvePackagePath(context.packageName(), entry.getValue())
                ));

        packageMap.putIfAbsent("service", packageMap.get("usecase"));
        packageMap.putIfAbsent("usecase", packageMap.get("service"));
        packageMap.putIfAbsent("repository-jpa", packageMap.get("repository-impl"));
        packageMap.putIfAbsent("repository", packageMap.get("port-out"));
        packageMap.putIfAbsent("port-out", packageMap.get("repository"));

        fileProps.put("pkg", packageMap);

        TemplateContext fileContext = new TemplateContext(
                context.packageName(), context.basePackage(), context.projectName(),
                context.entityName(), context.architecture(), context.javaVersion(),
                context.buildTool(), context.features(), fileProps
        );

        String content = templateService.renderJavaClass(templateName, fileContext);
        fileSystemService.writeFile(directory.resolve(fileName), content);
    }

    private String resolvePackagePath(String rootPackage, String path) {
        String cleanedPath = path.replace("{feature}", "demo").replace("/", ".");
        return rootPackage + (cleanedPath.isEmpty() ? "" : "." + cleanedPath);
    }

    private void generateFeatureFiles(ProjectConfig config, Path basePackagePath, TemplateContext context) throws IOException {
        Architecture arch = context.architecture();
        ProjectFeatures features = config.features();

        for (Architecture.FeatureBlueprint blueprint : arch.getFeatureBlueprints()) {

            if (blueprint.toggle().isEnabled(features)) {

                generateFeatureClass(
                        arch,
                        basePackagePath,
                        context,
                        blueprint.layer(),
                        blueprint.filename(),
                        blueprint.template()
                );
            }
        }
    }

    private void generateFeatureClass(Architecture arch, Path basePackagePath, TemplateContext context, String layerName, String fileName, String templateName) throws IOException {

        String layerPath = arch.getPathForLayer(layerName);
        String relativePath = layerPath.replace("{feature}", "demo").replace('.', '/');

        generateClass(basePackagePath, relativePath, fileName, templateName, context);
    }

    private void injectDependencies(ProjectConfig config, Path projectRoot) throws IOException {
        Path pomPath = projectRoot.resolve("pom.xml");
        Path gradlePath = projectRoot.resolve("build.gradle");
        Path gradleKtsPath = projectRoot.resolve("build.gradle.kts");

        if (Files.exists(pomPath)) {
            log.info("Enhancing Maven pom.xml with complete auto-configuration");
            String pomContent = fileSystemService.readFile(pomPath);
            String enhancedPom = pomManipulationService.enhancePomFile(pomContent, config);
            fileSystemService.writeFile(pomPath, enhancedPom);
            log.info("Maven pom.xml enhanced successfully");
        } else if (Files.exists(gradlePath)) {
            log.info("Enhancing Gradle build.gradle with complete auto-configuration");
            String gradleContent = fileSystemService.readFile(gradlePath);
            String enhancedGradle = gradleManipulationService.enhanceGradleFile(gradleContent, config);
            fileSystemService.writeFile(gradlePath, enhancedGradle);
            log.info("Gradle build.gradle enhanced successfully");
        } else if (Files.exists(gradleKtsPath)) {
            log.info("Enhancing Gradle build.gradle.kts with complete auto-configuration");
            String gradleContent = fileSystemService.readFile(gradleKtsPath);
            String enhancedGradle = gradleManipulationService.enhanceGradleFile(gradleContent, config);
            fileSystemService.writeFile(gradleKtsPath, enhancedGradle);
            log.info("Gradle build.gradle.kts enhanced successfully");
        }
    }

    private void generateConfigFiles(ProjectConfig config, Path projectRoot) throws IOException {
        Path resourcesPath = projectRoot.resolve("src/main/resources");
        TemplateContext context = buildTemplateContext(config);

        fileSystemService.writeFile(resourcesPath.resolve("application.yml"), templateService.renderConfig("application", context));
        fileSystemService.writeFile(resourcesPath.resolve("application-dev.yml"), templateService.renderConfig("application-dev", context));
        fileSystemService.writeFile(resourcesPath.resolve("application-prod.yml"), templateService.renderConfig("application-prod", context));
        fileSystemService.writeFile(resourcesPath.resolve("application-test.yml"), templateService.renderConfig("application-test", context));
    }

    private void generateDockerFiles(ProjectConfig config, Path projectRoot) throws IOException {
        TemplateContext context = buildTemplateContext(config);
        fileSystemService.writeFile(projectRoot.resolve("Dockerfile"), templateService.renderOps("Dockerfile", context));
        fileSystemService.writeFile(projectRoot.resolve("docker-compose.yml"), templateService.renderOps("docker-compose", context));
    }

    private void generateKubernetesFiles(ProjectConfig config, Path projectRoot) throws IOException {
        Path k8sDir = projectRoot.resolve("k8s");
        fileSystemService.createDirectories(k8sDir);
        TemplateContext context = buildTemplateContext(config);
        fileSystemService.writeFile(k8sDir.resolve("deployment.yml"), templateService.renderOps("kubernetes-deployment", context));
    }

    private void generateCiCdFiles(ProjectConfig config, Path projectRoot) throws IOException {
        Path githubDir = projectRoot.resolve(".github/workflows");
        fileSystemService.createDirectories(githubDir);
        TemplateContext context = buildTemplateContext(config);
        fileSystemService.writeFile(githubDir.resolve("ci.yml"), templateService.renderOps("github-actions", context));
    }

    private void generateGitignore(ProjectConfig config, Path projectRoot) throws IOException {
        TemplateContext context = buildTemplateContext(config);
        fileSystemService.writeFile(projectRoot.resolve(".gitignore"), templateService.renderOps("gitignore", context));
    }

    private TemplateContext buildTemplateContext(ProjectConfig config) {
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("database", detectDatabase(config));

        return TemplateContext.builder()
                .packageName(config.packageName())
                .basePackage(config.packageName())
                .projectName(config.artifactId())
                .entityName("Demo")
                .architecture(config.architecture())
                .javaVersion(config.javaVersion())
                .buildTool(config.buildTool())
                .features(config.features())
                .additionalProperties(additionalProps)
                .build();
    }

    private String detectDatabase(ProjectConfig config) {
        if (config.dependencies().contains("h2")) return "h2";
        if (config.dependencies().contains("postgresql")) return "postgresql";
        if (config.dependencies().contains("mysql")) return "mysql";
        if (config.dependencies().contains("mongodb")) return "mongodb";
        return null;
    }

    private Path resolveRealProjectRoot(Path baseOutputDir, String artifactId) throws IOException {
        Path expectedPath = baseOutputDir.resolve(artifactId);

        if (Files.exists(expectedPath) &&
                (Files.exists(expectedPath.resolve("pom.xml")) || Files.exists(expectedPath.resolve("build.gradle")))) {
            return expectedPath;
        }

        try (Stream<Path> stream = Files.list(baseOutputDir)) {
            Optional<Path> projectDir = stream
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().equals(artifactId))
                    .findFirst();

            if (projectDir.isPresent()) {
                return projectDir.get();
            }
        }

        throw new IOException("Could not find project root directory for: " + artifactId);
    }

    private Path findJavaSourceRoot(Path projectRoot) throws IOException {
        Path srcMainJava = projectRoot.resolve("src/main/java");
        if (Files.exists(srcMainJava)) {
            return srcMainJava;
        }
        throw new IOException("Could not find src/main/java directory in " + projectRoot);
    }
}