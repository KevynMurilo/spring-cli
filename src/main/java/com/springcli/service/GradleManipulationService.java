package com.springcli.service;

import com.springcli.model.ProjectConfig;
import com.springcli.model.ProjectFeatures;
import com.springcli.model.rules.GradleConfig;
import com.springcli.service.DependencyVersionResolver.LibraryVersions;
import com.springcli.service.config.BuildPluginConfigurationService;
import com.springcli.service.config.BuildPluginConfigurationService.GradlePlugin;
import com.springcli.service.config.DependencyConfigurationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradleManipulationService {

    private final DependencyVersionResolver versionResolver;
    private final BuildPluginConfigurationService pluginConfigService;
    private final DependencyConfigurationRegistry configRegistry;

    public String enhanceGradleFile(String buildContent, ProjectConfig config) {
        log.info("Enhancing build.gradle with complete auto-configuration");

        LibraryVersions versions = versionResolver.resolveVersions(config.springBootVersion());
        ProjectFeatures features = config.features();

        String enhanced = buildContent;
        enhanced = ensurePlugins(enhanced, config);
        enhanced = ensureSpringBootBom(enhanced, config.springBootVersion());
        enhanced = injectFeatureDependencies(enhanced, features, versions);
        enhanced = configureAnnotationProcessors(enhanced, config, versions);
        enhanced = ensureTestConfiguration(enhanced, versions);
        enhanced = cleanupWhitespace(enhanced);

        log.info("Build.gradle enhancement completed successfully");
        return enhanced;
    }

    private String ensurePlugins(String buildContent, ProjectConfig config) {
        if (!buildContent.contains("plugins {")) {
            List<GradlePlugin> plugins = pluginConfigService.generateGradlePlugins(
                    config.springBootVersion(),
                    config.dependencies(),
                    config.features()
            );

            StringBuilder pluginsBlock = new StringBuilder("plugins {\n");
            for (GradlePlugin plugin : plugins) {
                if (plugin.version() != null) {
                    pluginsBlock.append("    id '").append(plugin.id())
                            .append("' version '").append(plugin.version()).append("'\n");
                } else {
                    pluginsBlock.append("    id '").append(plugin.id()).append("'\n");
                }
            }
            pluginsBlock.append("}\n\n");

            return pluginsBlock + buildContent;
        }

        return buildContent;
    }

    private String ensureSpringBootBom(String buildContent, String springBootVersion) {
        if (!buildContent.contains("org.springframework.boot:spring-boot-dependencies")) {
            int dependencyManagementPos = buildContent.indexOf("dependencyManagement {");

            if (dependencyManagementPos == -1) {
                String bomSection = """

                        dependencyManagement {
                            imports {
                                mavenBom "org.springframework.boot:spring-boot-dependencies:%s"
                            }
                        }

                        """.formatted(springBootVersion);

                int dependenciesPos = buildContent.indexOf("dependencies {");
                if (dependenciesPos != -1) {
                    return buildContent.substring(0, dependenciesPos) + bomSection + buildContent.substring(dependenciesPos);
                }
            }
        }

        return buildContent;
    }

    private String injectFeatureDependencies(String buildContent, ProjectFeatures features, LibraryVersions versions) {
        int dependenciesEnd = findDependenciesBlock(buildContent);
        if (dependenciesEnd == -1) {
            log.warn("Could not find dependencies block in build.gradle");
            return buildContent;
        }

        List<String> featureDependencies = getActiveFeaturesAsDependencyIds(features);

        StringBuilder injections = new StringBuilder();

        for (String dependencyId : featureDependencies) {
            configRegistry.getRule(dependencyId).ifPresent(rule -> {
                if (rule.build() != null && rule.build().gradle() != null) {
                    injections.append(generateGradleDependencies(rule.build().gradle()));
                }
            });
        }

        if (injections.length() > 0) {
            return buildContent.substring(0, dependenciesEnd) + "\n" + injections + buildContent.substring(dependenciesEnd);
        }

        return buildContent;
    }

    private List<String> getActiveFeaturesAsDependencyIds(ProjectFeatures features) {
        List<String> dependencies = new ArrayList<>();

        if (features.enableJwt()) {
            dependencies.add("jwt");
        }
        if (features.enableSwagger()) {
            dependencies.add("swagger");
        }
        if (features.enableMapStruct()) {
            dependencies.add("mapstruct");
        }

        return dependencies;
    }

    private String generateGradleDependencies(GradleConfig gradle) {
        StringBuilder deps = new StringBuilder();

        if (gradle.implementation() != null) {
            gradle.implementation().forEach(dep ->
                deps.append("    implementation \"").append(dep).append("\"\n")
            );
        }

        if (gradle.compileOnly() != null) {
            gradle.compileOnly().forEach(dep ->
                deps.append("    compileOnly \"").append(dep).append("\"\n")
            );
        }

        if (gradle.runtimeOnly() != null) {
            gradle.runtimeOnly().forEach(dep ->
                deps.append("    runtimeOnly \"").append(dep).append("\"\n")
            );
        }

        if (gradle.annotationProcessor() != null) {
            gradle.annotationProcessor().forEach(dep ->
                deps.append("    annotationProcessor \"").append(dep).append("\"\n")
            );
        }

        return deps.toString();
    }

    private String configureAnnotationProcessors(String buildContent, ProjectConfig config, LibraryVersions versions) {
        List<AnnotationProcessor> processors = determineAnnotationProcessors(buildContent, config, versions);

        if (processors.isEmpty()) {
            return buildContent;
        }

        log.info("Configuring {} annotation processors for Gradle", processors.size());

        int dependenciesEnd = findDependenciesBlock(buildContent);
        if (dependenciesEnd == -1) {
            log.warn("Could not find dependencies block to inject annotation processors");
            return buildContent;
        }

        StringBuilder processorsBlock = new StringBuilder();
        for (AnnotationProcessor processor : processors) {
            processorsBlock.append(formatAnnotationProcessor(processor));
        }

        if (!buildContent.substring(0, dependenciesEnd).contains("annotationProcessor")) {
            return buildContent.substring(0, dependenciesEnd) + "\n" + processorsBlock + buildContent.substring(dependenciesEnd);
        }

        return buildContent;
    }

    private List<AnnotationProcessor> determineAnnotationProcessors(String buildContent, ProjectConfig config, LibraryVersions versions) {
        List<AnnotationProcessor> processors = new ArrayList<>();
        Set<String> dependencies = config.dependencies();
        ProjectFeatures features = config.features();

        boolean hasLombok = buildContent.contains("lombok");
        boolean hasMapStruct = features.enableMapStruct();
        boolean hasQueryDsl = dependencies.stream().anyMatch(dep -> dep.contains("querydsl"));
        boolean hasHibernateModelGen = dependencies.stream().anyMatch(dep -> dep.contains("hibernate") && dep.contains("jpamodelgen"));
        boolean hasConfigProcessor = buildContent.contains("spring-boot-configuration-processor");

        if (hasLombok && hasMapStruct) {
            processors.add(new AnnotationProcessor("org.projectlombok", "lombok", "1.18.36", null));
            processors.add(new AnnotationProcessor("org.projectlombok", "lombok-mapstruct-binding", versions.lombokMapstructBindingVersion(), null));
            processors.add(new AnnotationProcessor("org.mapstruct", "mapstruct-processor", versions.mapStructVersion(), null));
        } else if (hasLombok) {
            processors.add(new AnnotationProcessor("org.projectlombok", "lombok", "1.18.36", null));
        } else if (hasMapStruct) {
            processors.add(new AnnotationProcessor("org.mapstruct", "mapstruct-processor", versions.mapStructVersion(), null));
        }

        if (hasQueryDsl) {
            String queryDslVersion = resolveQueryDslVersion(versions);
            processors.add(new AnnotationProcessor("com.querydsl", "querydsl-apt", queryDslVersion, "jakarta"));
            processors.add(new AnnotationProcessor("jakarta.persistence", "jakarta.persistence-api", "3.1.0", null));
        }

        if (hasHibernateModelGen) {
            processors.add(new AnnotationProcessor("org.hibernate.orm", "hibernate-jpamodelgen", null, null));
        }

        if (hasConfigProcessor && !processors.isEmpty()) {
            processors.add(new AnnotationProcessor("org.springframework.boot", "spring-boot-configuration-processor", null, null));
        }

        return processors;
    }

    private String resolveQueryDslVersion(LibraryVersions versions) {
        return "5.1.0";
    }

    private String formatAnnotationProcessor(AnnotationProcessor processor) {
        if (processor.classifier != null) {
            return "    annotationProcessor \"%s:%s:%s:%s\"\n".formatted(
                    processor.groupId, processor.artifactId, processor.version, processor.classifier
            );
        } else if (processor.version != null) {
            return "    annotationProcessor \"%s:%s:%s\"\n".formatted(
                    processor.groupId, processor.artifactId, processor.version
            );
        } else {
            return "    annotationProcessor \"%s:%s\"\n".formatted(
                    processor.groupId, processor.artifactId
            );
        }
    }

    private String ensureTestConfiguration(String buildContent, LibraryVersions versions) {
        if (!buildContent.contains("test {")) {
            String testConfig = """

                    test {
                        useJUnitPlatform()
                        testLogging {
                            events "passed", "skipped", "failed"
                        }
                    }
                    """;

            int pos = buildContent.lastIndexOf("}");
            if (pos != -1) {
                return buildContent.substring(0, pos) + testConfig + buildContent.substring(pos);
            }
        }

        return buildContent;
    }

    private int findDependenciesBlock(String gradle) {
        int dependenciesBlock = gradle.indexOf("dependencies {");
        if (dependenciesBlock == -1) return -1;

        int braceCount = 0;
        int pos = dependenciesBlock + "dependencies {".length();

        while (pos < gradle.length()) {
            char c = gradle.charAt(pos);
            if (c == '{') braceCount++;
            if (c == '}') {
                if (braceCount == 0) return pos;
                braceCount--;
            }
            pos++;
        }

        return -1;
    }

    private String cleanupWhitespace(String buildContent) {
        return buildContent
                .replaceAll("\n{3,}", "\n\n")
                .replaceAll(" {2,}", " ");
    }

    private record AnnotationProcessor(String groupId, String artifactId, String version, String classifier) {}
}
