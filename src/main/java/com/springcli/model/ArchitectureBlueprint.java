package com.springcli.model;

public record ArchitectureBlueprint(
        String layer,           // Ex: "controller", "model", "config"
        String template,        // Ex: "common/Controller", "clean/UseCase"
        String filenameSuffix   // Ex: "Controller.java", "UseCase.java", ".java"
) {}    // ⭐ NOVO generateFeatureFiles: Agora dinâmico e baseado na Architecture
