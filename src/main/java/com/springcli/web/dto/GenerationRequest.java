package com.springcli.web.dto;

import com.springcli.model.ProjectFeatures;
import java.util.Set;

public record GenerationRequest(
        String groupId,
        String artifactId,
        String projectName,
        String description,
        String packageName,
        String springBootVersion,
        String javaVersion,
        String buildTool,
        String architecture,
        Set<String> dependencies,
        ProjectFeatures features,
        String outputPath
) {}