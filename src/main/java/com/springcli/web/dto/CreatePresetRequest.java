package com.springcli.web.dto;

import com.springcli.model.ProjectFeatures;

import java.util.Set;

public record CreatePresetRequest(
        String name,
        String description,
        String architecture,
        String javaVersion,
        Set<String> dependencies,
        ProjectFeatures features
) {}
