package com.springcli.model.rules;

public record BuildConfig(
    MavenConfig maven,
    GradleConfig gradle
) {
}
