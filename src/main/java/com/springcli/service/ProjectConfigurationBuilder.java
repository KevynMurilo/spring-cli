package com.springcli.service;

import com.springcli.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProjectConfigurationBuilder {

    public ProjectConfig buildFromPreset(
            Preset preset,
            String groupId,
            String artifactId,
            String name,
            String description,
            String packageName,
            String springBootVersion,
            String buildTool,
            String javaVersion,
            String packaging,
            Architecture architecture,
            Set<String> dependencies,
            ProjectFeatures features,
            String outputDirectory
    ) {
        return new ProjectConfig(
                groupId,
                artifactId,
                name,
                description,
                packageName,
                javaVersion,
                buildTool,
                packaging,
                architecture,
                springBootVersion,
                dependencies,
                features,
                outputDirectory
        );
    }

    public ProjectConfig buildFromScratch(
            String groupId,
            String artifactId,
            String name,
            String description,
            String packageName,
            String springBootVersion,
            String buildTool,
            String javaVersion,
            String packaging,
            Architecture architecture,
            Set<String> dependencies,
            ProjectFeatures features,
            String outputDirectory
    ) {
        return new ProjectConfig(
                groupId,
                artifactId,
                name,
                description,
                packageName,
                javaVersion,
                buildTool,
                packaging,
                architecture,
                springBootVersion,
                dependencies,
                features,
                outputDirectory
        );
    }

    public ProjectConfig buildQuickProject(
            String artifactId,
            String groupId,
            Architecture architecture,
            String outputDirectory,
            String springBootVersion,
            String buildTool
    ) {
        return new ProjectConfig(
                groupId,
                artifactId,
                artifactId,
                "Spring Boot Application",
                groupId + "." + artifactId.replace("-", ""),
                "21",
                buildTool,
                "jar",
                architecture,
                springBootVersion,
                Set.of("web", "lombok"),
                ProjectFeatures.defaults(),
                outputDirectory
        );
    }
}
