package com.springcli.model;

import java.util.Set;

public record ProjectConfig(
        String groupId,
        String artifactId,
        String name,
        String description,
        String packageName,
        String javaVersion,
        String buildTool,
        String packaging,
        Architecture architecture,
        String springBootVersion,
        Set<String> dependencies,
        ProjectFeatures features,
        String outputDirectory
) {
    public ProjectConfig {
        if (groupId == null || groupId.isBlank()) throw new IllegalArgumentException("groupId cannot be empty");
        if (artifactId == null || artifactId.isBlank()) throw new IllegalArgumentException("artifactId cannot be empty");
        if (packageName == null || packageName.isBlank()) packageName = groupId + "." + artifactId.replace("-", "");
        if (name == null || name.isBlank()) name = artifactId;
        if (description == null) description = "Spring Boot Application";
    }
}