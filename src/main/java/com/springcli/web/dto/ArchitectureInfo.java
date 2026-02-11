package com.springcli.web.dto;

import java.util.Map;

public record ArchitectureInfo(String name, String displayName, String description, Map<String, String> layers) {}