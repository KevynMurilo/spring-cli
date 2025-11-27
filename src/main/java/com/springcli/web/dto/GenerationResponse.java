package com.springcli.web.dto;

public record GenerationResponse(
        boolean success,
        String message,
        String projectPath
) {}