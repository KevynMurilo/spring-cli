package com.springcli.model.rules;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DependencyRule(
    String id,
    String category,
    int priority,
    BuildConfig build,
    RuntimeConfig runtime,
    InfrastructureConfig infrastructure,
    ScaffoldingConfig scaffolding
) {
}
