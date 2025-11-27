package com.springcli.model.rules;

import java.util.List;

public record GradleConfig(
    List<String> implementation,
    List<String> compileOnly,
    List<String> runtimeOnly,
    List<String> annotationProcessor,
    List<String> compilerOptions,
    List<String> plugins
) {
}
