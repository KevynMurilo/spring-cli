package com.springcli.model;

import java.util.HashMap;
import java.util.Map;

public record TemplateContext(
    String packageName,
    String basePackage,
    String projectName,
    String entityName,
    Architecture architecture,
    String javaVersion,
    String buildTool,
    ProjectFeatures features,
    Map<String, Object> additionalProperties
) {
    public TemplateContext {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String packageName;
        private String basePackage;
        private String projectName;
        private String entityName = "Demo";
        private Architecture architecture;
        private String javaVersion;
        private String buildTool;
        private ProjectFeatures features = ProjectFeatures.defaults();
        private Map<String, Object> additionalProperties = new HashMap<>();

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder basePackage(String basePackage) {
            this.basePackage = basePackage;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }

        public Builder architecture(Architecture architecture) {
            this.architecture = architecture;
            return this;
        }

        public Builder javaVersion(String javaVersion) {
            this.javaVersion = javaVersion;
            return this;
        }

        public Builder buildTool(String buildTool) {
            this.buildTool = buildTool;
            return this;
        }

        public Builder features(ProjectFeatures features) {
            this.features = features;
            return this;
        }

        public Builder additionalProperties(Map<String, Object> additionalProperties) {
            this.additionalProperties = additionalProperties;
            return this;
        }

        public Builder addProperty(String key, Object value) {
            this.additionalProperties.put(key, value);
            return this;
        }

        public TemplateContext build() {
            return new TemplateContext(
                packageName,
                basePackage,
                projectName,
                entityName,
                architecture,
                javaVersion,
                buildTool,
                features,
                additionalProperties
            );
        }
    }
}
