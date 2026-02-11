package com.springcli.model;

import java.util.Set;

public enum ProjectPreset {

    REST_API(
            "REST-API",
            "Clean Architecture REST API",
            Architecture.CLEAN,
            "21",
            Set.of("web", "data-jpa", "h2", "validation", "lombok", "devtools"),
            new ProjectFeatures(true, true, true, true, true, false, false, false, true)
    ),

    GRAPHQL_API(
            "GraphQL-API",
            "GraphQL API with Spring for GraphQL",
            Architecture.CLEAN,
            "21",
            Set.of("web", "graphql", "data-jpa", "h2", "validation", "lombok", "devtools"),
            new ProjectFeatures(true, false, true, true, true, false, false, false, true)
    ),

    MICROSERVICE(
            "Microservice",
            "Hexagonal architecture microservice",
            Architecture.HEXAGONAL,
            "21",
            Set.of("web", "data-jpa", "postgresql", "cloud-eureka", "cloud-config-client", "actuator", "lombok"),
            new ProjectFeatures(true, true, true, true, true, true, true, true, true)
    ),

    MONOLITH(
            "Monolith",
            "Traditional MVC monolith",
            Architecture.MVC,
            "21",
            Set.of("web", "thymeleaf", "data-jpa", "mysql", "security", "validation", "lombok"),
            new ProjectFeatures(false, false, false, true, false, true, false, false, false)
    ),

    REACTIVE_API(
            "Reactive-API",
            "Reactive WebFlux API with R2DBC",
            Architecture.CLEAN,
            "21",
            Set.of("webflux", "data-r2dbc", "h2", "validation", "lombok", "devtools"),
            new ProjectFeatures(false, true, true, true, false, true, false, false, false)
    ),

    EVENT_DRIVEN_SERVICE(
            "Event-Driven",
            "Event-driven microservice with Kafka",
            Architecture.EVENT_DRIVEN,
            "21",
            Set.of("web", "kafka", "data-jpa", "postgresql", "actuator", "lombok"),
            new ProjectFeatures(false, true, true, true, true, true, true, true, true)
    ),

    DDD_SERVICE(
            "DDD-Service",
            "Domain-Driven Design service",
            Architecture.DDD,
            "21",
            Set.of("web", "data-jpa", "postgresql", "validation", "lombok", "devtools"),
            new ProjectFeatures(false, true, true, true, true, true, false, false, true)
    ),

    MINIMAL(
            "Minimal",
            "Minimal Spring Boot app",
            Architecture.MVC,
            "21",
            Set.of("web", "lombok", "devtools"),
            ProjectFeatures.defaults()
    );

    private final String name;
    private final String description;
    private final Architecture architecture;
    private final String javaVersion;
    private final Set<String> dependencies;
    private final ProjectFeatures features;

    ProjectPreset(String name, String description, Architecture architecture,
                  String javaVersion, Set<String> dependencies, ProjectFeatures features) {
        this.name = name;
        this.description = description;
        this.architecture = architecture;
        this.javaVersion = javaVersion;
        this.dependencies = dependencies;
        this.features = features;
    }

    public Preset toPreset() {
        return new Preset(name, description, architecture, javaVersion, dependencies, features, true);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Architecture getArchitecture() {
        return architecture;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public ProjectFeatures getFeatures() {
        return features;
    }
}
