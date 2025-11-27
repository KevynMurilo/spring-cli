package com.springcli.model.rules;

public record MavenExclusion(
    String groupId,
    String artifactId
) {
}
