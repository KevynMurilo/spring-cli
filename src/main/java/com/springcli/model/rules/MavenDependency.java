package com.springcli.model.rules;

public record MavenDependency(
    String groupId,
    String artifactId,
    String scope,
    String version
) {
}
