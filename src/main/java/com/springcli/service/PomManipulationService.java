package com.springcli.service;

import com.springcli.model.ProjectFeatures;
import com.springcli.service.DependencyVersionResolver.LibraryVersions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PomManipulationService {

    private final DependencyVersionResolver versionResolver;

    public String injectDependencies(String pomContent, ProjectFeatures features, String springBootVersion) {
        LibraryVersions versions = versionResolver.resolveVersions(springBootVersion);

        StringBuilder result = new StringBuilder(pomContent);

        result = new StringBuilder(ensureSpringBootBom(result.toString(), springBootVersion));

        int dependenciesEnd = findDependenciesEndTag(result.toString());
        if (dependenciesEnd == -1) {
            log.warn("Could not find </dependencies> tag in pom.xml");
            return result.toString();
        }

        StringBuilder injections = new StringBuilder();

        if (features.enableJwt()) {
            injections.append(getJwtDependencies(versions.jjwtVersion()));
        }

        if (features.enableSwagger()) {
            injections.append(getSwaggerDependency(versions.springDocVersion()));
        }

        if (features.enableMapStruct()) {
            injections.append(getMapStructDependency(versions.mapStructVersion()));
        }

        if (injections.length() > 0) {
            result.insert(dependenciesEnd, injections.toString());
        }

        if (features.enableMapStruct()) {
            result = new StringBuilder(injectMapStructProcessor(result.toString(), versions.mapStructVersion()));
        }

        return result.toString();
    }

    private int findDependenciesEndTag(String pom) {
        return pom.indexOf("</dependencies>");
    }

    private String ensureSpringBootBom(String pomContent, String springBootVersion) {
        if (pomContent.contains("<dependencyManagement>")) {
            log.debug("dependencyManagement section already exists");
            return pomContent;
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

    private String getJwtDependencies(String jjwtVersion) {
        return """
                    <!-- JWT Dependencies -->
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-api</artifactId>
                        <version>%s</version>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-impl</artifactId>
                        <version>%s</version>
                        <scope>runtime</scope>
                    </dependency>
                    <dependency>
                        <groupId>io.jsonwebtoken</groupId>
                        <artifactId>jjwt-jackson</artifactId>
                        <version>%s</version>
                        <scope>runtime</scope>
                    </dependency>
                """.formatted(jjwtVersion, jjwtVersion, jjwtVersion);
    }

    private String getSwaggerDependency(String springDocVersion) {
        return """
                    <!-- SpringDoc OpenAPI -->
                    <dependency>
                        <groupId>org.springdoc</groupId>
                        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                        <version>%s</version>
                    </dependency>
                """.formatted(springDocVersion);
    }

    private String getMapStructDependency(String mapStructVersion) {
        return """
                    <!-- MapStruct -->
                    <dependency>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct</artifactId>
                        <version>%s</version>
                    </dependency>
                """.formatted(mapStructVersion);
    }

    private String injectMapStructProcessor(String pomContent, String mapStructVersion) {
        if (pomContent.contains("<annotationProcessorPaths>")) {
            String processorPath = """
                                        <path>
                                            <groupId>org.mapstruct</groupId>
                                            <artifactId>mapstruct-processor</artifactId>
                                            <version>%s</version>
                                        </path>
                    """.formatted(mapStructVersion);

            int processorPathsEnd = pomContent.indexOf("</annotationProcessorPaths>");
            if (processorPathsEnd != -1) {
                return pomContent.substring(0, processorPathsEnd) + processorPath + pomContent.substring(processorPathsEnd);
            }
        } else {
            return injectMapStructPluginConfiguration(pomContent, mapStructVersion);
        }

        return pomContent;
    }

    private String injectMapStructPluginConfiguration(String pomContent, String mapStructVersion) {
        String mapStructPlugin = """
                <build>
                    <plugins>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <version>3.11.0</version>
                            <configuration>
                                <annotationProcessorPaths>
                                    <path>
                                        <groupId>org.mapstruct</groupId>
                                        <artifactId>mapstruct-processor</artifactId>
                                        <version>%s</version>
                                    </path>
                                    <path>
                                        <groupId>org.projectlombok</groupId>
                                        <artifactId>lombok</artifactId>
                                        <version>${lombok.version}</version>
                                    </path>
                                    <path>
                                        <groupId>org.projectlombok</groupId>
                                        <artifactId>lombok-mapstruct-binding</artifactId>
                                        <version>0.2.0</version>
                                    </path>
                                </annotationProcessorPaths>
                            </configuration>
                        </plugin>
                    </plugins>
                </build>
                """.formatted(mapStructVersion);

        int projectEnd = pomContent.indexOf("</project>");
        if (projectEnd != -1) {
            return pomContent.substring(0, projectEnd) + mapStructPlugin + pomContent.substring(projectEnd);
        }

        return pomContent;
    }
}