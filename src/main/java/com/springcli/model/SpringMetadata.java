package com.springcli.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public record SpringMetadata(
        String defaultSpringBootVersion,
        List<String> springBootVersions,
        List<String> javaVersions,
        List<String> packagingTypes,
        List<BuildToolOption> buildTools,
        String defaultBuildTool,
        List<String> languages,
        String defaultLanguage,
        Map<String, DependencyGroup> dependencyGroups,
        long cachedAt
) {
    public SpringMetadata {
        if (springBootVersions == null) springBootVersions = new ArrayList<>();
        if (javaVersions == null) javaVersions = new ArrayList<>();
        if (packagingTypes == null) packagingTypes = new ArrayList<>();
        if (buildTools == null) buildTools = new ArrayList<>();
        if (languages == null) languages = new ArrayList<>();
        if (dependencyGroups == null) dependencyGroups = new HashMap<>();
    }

    public boolean isExpired(long maxAgeMillis) {
        return System.currentTimeMillis() - cachedAt > maxAgeMillis;
    }
}