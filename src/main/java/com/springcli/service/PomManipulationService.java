package com.springcli.service;

import com.springcli.model.ProjectConfig;
import com.springcli.model.ProjectFeatures;
import com.springcli.model.rules.DependencyRule;
import com.springcli.model.rules.MavenDependency;
import com.springcli.service.DependencyVersionResolver.LibraryVersions;
import com.springcli.service.config.BuildPluginConfigurationService;
import com.springcli.service.config.BuildPluginConfigurationService.MavenPlugin;
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
public class PomManipulationService {

    private final DependencyVersionResolver versionResolver;
    private final BuildPluginConfigurationService pluginConfigService;
    private final DependencyConfigurationRegistry configRegistry;

    public String enhancePomFile(String pomContent, ProjectConfig config) {
        log.info("Enhancing pom.xml with complete auto-configuration");

        LibraryVersions versions = versionResolver.resolveVersions(config.springBootVersion());
        ProjectFeatures features = config.features();

        String enhanced = pomContent;
        enhanced = ensureProperties(enhanced, config.javaVersion(), versions);
        enhanced = ensureSpringBootBom(enhanced, config.springBootVersion());
        enhanced = injectFeatureDependencies(enhanced, features, versions);
        enhanced = ensurePluginsSection(enhanced);
        enhanced = configureAnnotationProcessors(enhanced, config, versions);
        enhanced = injectPlugins(enhanced, config);
        enhanced = cleanupWhitespace(enhanced);

        log.info("Pom.xml enhancement completed successfully");
        return enhanced;
    }

    private String ensureProperties(String pomContent, String javaVersion, LibraryVersions versions) {
        if (pomContent.contains("<properties>")) {
            int propertiesEnd = pomContent.indexOf("</properties>");
            if (propertiesEnd != -1) {
                int propertiesStart = pomContent.indexOf("<properties>") + "<properties>".length();
                String existingProps = pomContent.substring(propertiesStart, propertiesEnd);

                if (existingProps.contains("<java.version>")) {
                    pomContent = pomContent.replaceAll("<java.version>.*?</java.version>", "<java.version>" + javaVersion + "</java.version>");
                }
                if (existingProps.contains("<maven.compiler.source>")) {
                    pomContent = pomContent.replaceAll("<maven.compiler.source>.*?</maven.compiler.source>", "<maven.compiler.source>" + javaVersion + "</maven.compiler.source>");
                }
                if (existingProps.contains("<maven.compiler.target>")) {
                    pomContent = pomContent.replaceAll("<maven.compiler.target>.*?</maven.compiler.target>", "<maven.compiler.target>" + javaVersion + "</maven.compiler.target>");
                }

                if (!existingProps.contains("<lombok.version>")) {
                    String newProps = "\n\t\t<lombok.version>1.18.36</lombok.version>";
                    pomContent = pomContent.substring(0, propertiesEnd) + newProps + pomContent.substring(propertiesEnd);
                }
            }
        } else {
            String properties = generatePropertiesSection(javaVersion, versions);
            int projectEnd = pomContent.indexOf("</project>");
            if (projectEnd != -1) {
                int dependenciesStart = pomContent.indexOf("<dependencies>");
                if (dependenciesStart != -1) {
                    return pomContent.substring(0, dependenciesStart) + properties + pomContent.substring(dependenciesStart);
                }
                return pomContent.substring(0, projectEnd) + properties + pomContent.substring(projectEnd);
            }
        }
        return pomContent;
    }

    private String generatePropertiesSection(String javaVersion, LibraryVersions versions) {
        return """
                    <properties>
                        <java.version>%s</java.version>
                        <maven.compiler.source>%s</maven.compiler.source>
                        <maven.compiler.target>%s</maven.compiler.target>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                        <lombok.version>1.18.36</lombok.version>
                    </properties>
                """.formatted(javaVersion, javaVersion, javaVersion);
    }

    private String ensureSpringBootBom(String pomContent, String springBootVersion) {
        if (pomContent.contains("spring-boot-dependencies") && pomContent.contains("<dependencyManagement>")) {
            log.debug("Spring Boot BOM already exists in dependencyManagement");
            return pomContent;
        }

        if (pomContent.contains("<dependencyManagement>")) {
            int dmEnd = pomContent.indexOf("</dependencyManagement>");
            int depsEnd = pomContent.lastIndexOf("</dependencies>", dmEnd);

            if (depsEnd != -1 && depsEnd < dmEnd) {
                String bomDependency = """
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-dependencies</artifactId>
                                <version>%s</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                    """.formatted(springBootVersion);

                return pomContent.substring(0, depsEnd) + bomDependency + pomContent.substring(depsEnd);
            }
        }

        String bomSection = """
                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-dependencies</artifactId>
                                <version>%s</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                        </dependencies>
                    </dependencyManagement>

                """.formatted(springBootVersion);

        int dependenciesStart = pomContent.indexOf("<dependencies>");
        if (dependenciesStart != -1) {
            return pomContent.substring(0, dependenciesStart) + bomSection + pomContent.substring(dependenciesStart);
        }

        log.warn("Could not find <dependencies> tag to inject BOM");
        return pomContent;
    }

    private String injectFeatureDependencies(String pomContent, ProjectFeatures features, LibraryVersions versions) {
        int lastDependenciesEnd = findLastDependenciesEndTag(pomContent);
        if (lastDependenciesEnd == -1) {
            log.warn("Could not find main </dependencies> tag in pom.xml");
            return pomContent;
        }

        List<String> featureDependencies = getActiveFeaturesAsDependencyIds(features);

        StringBuilder injections = new StringBuilder();

        for (String dependencyId : featureDependencies) {
            configRegistry.getRule(dependencyId).ifPresent(rule -> {
                if (rule.build() != null && rule.build().maven() != null &&
                    rule.build().maven().dependencies() != null &&
                    !rule.build().maven().dependencies().isEmpty()) {
                    injections.append(generateMavenDependenciesXml(rule.build().maven().dependencies()));
                }
            });
        }

        if (injections.length() > 0) {
            return pomContent.substring(0, lastDependenciesEnd) + injections + pomContent.substring(lastDependenciesEnd);
        }

        return pomContent;
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

    private String generateMavenDependenciesXml(List<MavenDependency> dependencies) {
        StringBuilder xml = new StringBuilder();
        for (MavenDependency dep : dependencies) {
            xml.append("        <dependency>\n");
            xml.append("            <groupId>").append(dep.groupId()).append("</groupId>\n");
            xml.append("            <artifactId>").append(dep.artifactId()).append("</artifactId>\n");

            if (dep.version() != null && !dep.version().isEmpty()) {
                xml.append("            <version>").append(dep.version()).append("</version>\n");
            }

            if (dep.scope() != null && !dep.scope().isEmpty()) {
                xml.append("            <scope>").append(dep.scope()).append("</scope>\n");
            }

            xml.append("        </dependency>\n");
        }
        return xml.toString();
    }

    private int findLastDependenciesEndTag(String pomContent) {
        int buildStart = pomContent.indexOf("<build>");
        if (buildStart == -1) {
            buildStart = pomContent.length();
        }

        int dependencyManagementStart = pomContent.indexOf("<dependencyManagement>");

        int searchStart = 0;
        if (dependencyManagementStart != -1) {
            int dependencyManagementEnd = pomContent.indexOf("</dependencyManagement>", dependencyManagementStart);
            if (dependencyManagementEnd != -1) {
                searchStart = dependencyManagementEnd;
            }
        }

        int dependenciesEnd = pomContent.indexOf("</dependencies>", searchStart);

        if (dependenciesEnd == -1 || dependenciesEnd > buildStart) {
            return -1;
        }

        return dependenciesEnd;
    }

    private String ensurePluginsSection(String pomContent) {
        if (!pomContent.contains("<build>")) {
            String buildSection = """

                        <build>
                            <plugins>
                            </plugins>
                        </build>
                    """;

            int projectEnd = pomContent.indexOf("</project>");
            if (projectEnd != -1) {
                return pomContent.substring(0, projectEnd) + buildSection + pomContent.substring(projectEnd);
            }
        } else if (!pomContent.contains("<plugins>")) {
            String pluginsSection = """
                            <plugins>
                            </plugins>
                    """;

            int buildEnd = pomContent.indexOf("</build>");
            if (buildEnd != -1) {
                return pomContent.substring(0, buildEnd) + pluginsSection + pomContent.substring(buildEnd);
            }
        }

        return pomContent;
    }

    private String configureAnnotationProcessors(String pomContent, ProjectConfig config, LibraryVersions versions) {
        List<AnnotationProcessor> processors = determineAnnotationProcessors(pomContent, config, versions);

        if (processors.isEmpty()) {
            return pomContent;
        }

        log.info("Configuring {} annotation processors for Maven Compiler Plugin", processors.size());

        if (!pomContent.contains("maven-compiler-plugin")) {
            String compilerPlugin = buildCompilerPluginWithProcessors(config.javaVersion(), processors);
            int pluginsEnd = pomContent.indexOf("</plugins>");
            if (pluginsEnd != -1) {
                return pomContent.substring(0, pluginsEnd) + compilerPlugin + pomContent.substring(pluginsEnd);
            }
        } else {
            return updateExistingCompilerPlugin(pomContent, processors);
        }

        return pomContent;
    }

    private List<AnnotationProcessor> determineAnnotationProcessors(String pomContent, ProjectConfig config, LibraryVersions versions) {
        List<AnnotationProcessor> processors = new ArrayList<>();
        Set<String> dependencies = config.dependencies();
        ProjectFeatures features = config.features();

        boolean hasLombok = pomContent.contains("lombok");
        boolean hasMapStruct = features.enableMapStruct();
        boolean hasQueryDsl = dependencies.stream().anyMatch(dep -> dep.contains("querydsl"));
        boolean hasHibernateModelGen = dependencies.stream().anyMatch(dep -> dep.contains("hibernate") && dep.contains("jpamodelgen"));
        boolean hasConfigProcessor = pomContent.contains("spring-boot-configuration-processor");

        if (hasLombok && hasMapStruct) {
            processors.add(new AnnotationProcessor("org.projectlombok", "lombok", "${lombok.version}", null));
            processors.add(new AnnotationProcessor("org.projectlombok", "lombok-mapstruct-binding", versions.lombokMapstructBindingVersion(), null));
            processors.add(new AnnotationProcessor("org.mapstruct", "mapstruct-processor", versions.mapStructVersion(), null));
        } else if (hasLombok) {
            processors.add(new AnnotationProcessor("org.projectlombok", "lombok", "${lombok.version}", null));
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

    private String buildCompilerPluginWithProcessors(String javaVersion, List<AnnotationProcessor> processors) {
        StringBuilder plugin = new StringBuilder("""
                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.13.0</version>
                                <configuration>
                                    <source>%s</source>
                                    <target>%s</target>
                                    <annotationProcessorPaths>
                """.formatted(javaVersion, javaVersion));

        for (AnnotationProcessor processor : processors) {
            plugin.append(formatProcessorPath(processor));
        }

        plugin.append("""
                                    </annotationProcessorPaths>
                                </configuration>
                            </plugin>
                """);

        return plugin.toString();
    }

    private String updateExistingCompilerPlugin(String pomContent, List<AnnotationProcessor> processors) {
        int compilerPluginStart = pomContent.indexOf("<artifactId>maven-compiler-plugin</artifactId>");
        if (compilerPluginStart == -1) {
            return pomContent;
        }

        int pluginStart = pomContent.lastIndexOf("<plugin>", compilerPluginStart);
        int pluginEnd = pomContent.indexOf("</plugin>", compilerPluginStart) + "</plugin>".length();

        if (pluginStart == -1 || pluginEnd == -1) {
            return pomContent;
        }

        String existingPlugin = pomContent.substring(pluginStart, pluginEnd);

        if (existingPlugin.contains("<annotationProcessorPaths>")) {
            int pathsStart = existingPlugin.indexOf("<annotationProcessorPaths>");
            int pathsEnd = existingPlugin.indexOf("</annotationProcessorPaths>") + "</annotationProcessorPaths>".length();

            StringBuilder newPaths = new StringBuilder("<annotationProcessorPaths>\n");
            for (AnnotationProcessor processor : processors) {
                newPaths.append(formatProcessorPath(processor));
            }
            newPaths.append("                        </annotationProcessorPaths>");

            String updatedPlugin = existingPlugin.substring(0, pathsStart) + newPaths + existingPlugin.substring(pathsEnd);
            return pomContent.substring(0, pluginStart) + updatedPlugin + pomContent.substring(pluginEnd);
        } else {
            int configEnd = existingPlugin.lastIndexOf("</configuration>");
            if (configEnd != -1) {
                StringBuilder paths = new StringBuilder("                        <annotationProcessorPaths>\n");
                for (AnnotationProcessor processor : processors) {
                    paths.append(formatProcessorPath(processor));
                }
                paths.append("                        </annotationProcessorPaths>\n                    ");

                String updatedPlugin = existingPlugin.substring(0, configEnd) + paths + existingPlugin.substring(configEnd);
                return pomContent.substring(0, pluginStart) + updatedPlugin + pomContent.substring(pluginEnd);
            }
        }

        return pomContent;
    }

    private String formatProcessorPath(AnnotationProcessor processor) {
        StringBuilder path = new StringBuilder("""
                                        <path>
                                            <groupId>%s</groupId>
                                            <artifactId>%s</artifactId>
                """.formatted(processor.groupId, processor.artifactId));

        if (processor.version != null) {
            path.append("                            <version>").append(processor.version).append("</version>\n");
        }

        if (processor.classifier != null) {
            path.append("                            <classifier>").append(processor.classifier).append("</classifier>\n");
        }

        path.append("                        </path>\n");
        return path.toString();
    }

    private String injectPlugins(String pomContent, ProjectConfig config) {
        List<MavenPlugin> plugins = pluginConfigService.generateMavenPlugins(
                config.springBootVersion(),
                config.dependencies(),
                config.features()
        );

        int pluginsEnd = pomContent.indexOf("</plugins>");
        if (pluginsEnd == -1) {
            log.warn("Could not find </plugins> tag in pom.xml");
            return pomContent;
        }

        StringBuilder pluginsXml = new StringBuilder();
        for (MavenPlugin plugin : plugins) {
            if (!pomContent.contains("<artifactId>" + plugin.artifactId() + "</artifactId>")) {
                pluginsXml.append("""
                            <plugin>
                                <groupId>%s</groupId>
                                <artifactId>%s</artifactId>
                                <version>%s</version>
                %s            </plugin>
                """.formatted(
                        plugin.groupId(),
                        plugin.artifactId(),
                        plugin.version(),
                        plugin.configuration() != null ? plugin.configuration() : ""
                ));
            }
        }

        if (pluginsXml.length() > 0) {
            return pomContent.substring(0, pluginsEnd) + pluginsXml + pomContent.substring(pluginsEnd);
        }

        return pomContent;
    }

    private String cleanupWhitespace(String pomContent) {
        return pomContent
                .replaceAll("\n{3,}", "\n\n")
                .replaceAll("(?m)^[ \\t]+$", "")
                .replaceAll("(?m)\\t+\\n", "\\n")
                .replaceAll("[ \\t]+\\n", "\\n")
                .replaceAll("</dependency>\\n+<dependency>", "</dependency>\\n\\t\\t<dependency>")
                .replaceAll("</plugin>\\n+<plugin>", "</plugin>\\n\\t\\t\\t<plugin>")
                .replaceAll("</property>\\n+<property>", "</property>\\n\\t\\t<property>")
                .trim();
    }

    private record AnnotationProcessor(String groupId, String artifactId, String version, String classifier) {}
}
