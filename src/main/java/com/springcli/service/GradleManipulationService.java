package com.springcli.service;

import com.springcli.model.ProjectFeatures;
import com.springcli.service.DependencyVersionResolver.LibraryVersions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradleManipulationService {

    private final DependencyVersionResolver versionResolver;

    public String injectDependencies(String buildContent, ProjectFeatures features, String springBootVersion) {
        LibraryVersions versions = versionResolver.resolveVersions(springBootVersion);

        StringBuilder result = new StringBuilder(buildContent);

        result = new StringBuilder(ensureSpringBootBom(result.toString(), springBootVersion));

        int dependenciesEnd = findDependenciesBlock(result.toString());
        if (dependenciesEnd == -1) {
            log.warn("Could not find dependencies block in build.gradle");
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
            result = new StringBuilder(injectMapStructConfiguration(result.toString(), versions));
        }

        return result.toString();
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

    private String ensureSpringBootBom(String buildContent, String springBootVersion) {
        if (!buildContent.contains("org.springframework.boot:spring-boot-dependencies")) {
            int dependencyManagementPos = buildContent.indexOf("dependencyManagement {");

            if (dependencyManagementPos == -1) {
                String bomSection = String.format("""

                        dependencyManagement {
                            imports {
                                mavenBom "org.springframework.boot:spring-boot-dependencies:%s"
                            }
                        }
                        """, springBootVersion);

                int dependenciesPos = buildContent.indexOf("dependencies {");
                if (dependenciesPos != -1) {
                    return buildContent.substring(0, dependenciesPos) + bomSection + "\n" + buildContent.substring(dependenciesPos);
                }
            }
        }

        return buildContent;
    }

    private String getJwtDependencies(String jjwtVersion) {
        return String.format("""
                    implementation "io.jsonwebtoken:jjwt-api:%s"
                    runtimeOnly "io.jsonwebtoken:jjwt-impl:%s"
                    runtimeOnly "io.jsonwebtoken:jjwt-jackson:%s"
                """, jjwtVersion, jjwtVersion, jjwtVersion);
    }

    private String getSwaggerDependency(String springDocVersion) {
        return String.format("""
                    implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:%s"
                """, springDocVersion);
    }

    private String getMapStructDependency(String mapStructVersion) {
        return String.format("""
                    implementation "org.mapstruct:mapstruct:%s"
                    annotationProcessor "org.mapstruct:mapstruct-processor:%s"
                """, mapStructVersion, mapStructVersion);
    }

    private String injectMapStructConfiguration(String buildContent, LibraryVersions versions) {
        if (buildContent.contains("annotationProcessor \"org.mapstruct:mapstruct-processor")) {
            String lombokBinding = String.format("""
                    annotationProcessor "org.projectlombok:lombok-mapstruct-binding:%s"
                    """, versions.lombokMapstructBindingVersion());

            int mapStructPos = buildContent.indexOf("annotationProcessor \"org.mapstruct:mapstruct-processor");
            int lineEnd = buildContent.indexOf("\n", mapStructPos);

            if (lineEnd != -1) {
                return buildContent.substring(0, lineEnd + 1) + lombokBinding + buildContent.substring(lineEnd + 1);
            }
        }

        return buildContent;
    }
}
