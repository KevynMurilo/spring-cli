package com.springcli.model;

import java.util.Set;

public record Preset(
        String name,
        String description,
        Architecture architecture,
        String javaVersion,
        Set<String> dependencies,
        ProjectFeatures features,
        boolean builtIn
) {}