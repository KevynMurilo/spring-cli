package com.springcli.model.rules;

public record MavenPlugin(
    String groupId,
    String artifactId,
    String executionGoal
) {
}
