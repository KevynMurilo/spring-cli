package com.springcli.model;

public record ProjectFeatures(
    boolean enableJwt,
    boolean enableSwagger,
    boolean enableCors,
    boolean enableExceptionHandler,
    boolean enableMapStruct,
    boolean enableDocker,
    boolean enableKubernetes,
    boolean enableCiCd,
    boolean enableAudit
) {
    public static ProjectFeatures defaults() {
        return new ProjectFeatures(false, false, false, false, false, false, false, false, false);
    }

    public static ProjectFeatures all() {
        return new ProjectFeatures(true, true, true, true, true, true, true, true, true);
    }
}
