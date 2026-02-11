package com.springcli.web.dto;

import java.util.List;

public record FeatureInfo(
        String id,
        String label,
        String description,
        String icon,
        String category,
        List<String> requiredDependencies
) {}
