# Spring CLI – Refactored Architecture

## Overview

Spring CLI has been fully refactored to use a JSON‑based system for dependency configuration, removing all hardcoded if/else logic that was previously scattered through the code.[1]

## Rule‑Based Architecture

### dependency-rules.json

Located at `src/main/resources/dependency-rules.json`, this file is the core of the system and defines all configuration rules for each supported dependency.[1]

#### Rule Structure

```json
{
  "id": "postgresql",
  "category": "DATA",
  "priority": 0,
  "build": {
    "maven": { ... },
    "gradle": { ... }
  },
  "runtime": {
    "properties": [ ... ]
  },
  "infrastructure": {
    "dockerCompose": { ... }
  },
  "scaffolding": {
    "files": [ ... ]
  }
}
```

### Main Components

#### 1. Data Models (`com.springcli.model.rules`)

- DependencyRule: Root model representing a complete rule.
- BuildConfig: Build configuration (Maven/Gradle).
- RuntimeConfig: Properties for application.yml.
- InfrastructureConfig: Infrastructure configuration (Docker Compose).[2]
- ScaffoldingConfig: Code files to be generated.[3]

#### 2. DependencyRulesService

Responsibilities:

- Load `dependency-rules.json` at startup.
- Provide access to rules by ID.
- Sort rules by priority.

```java
@Service
public class DependencyRulesService {
    public Optional<DependencyRule> getRule(String dependencyId);
    public List<DependencyRule> getRules(List<String> dependencyIds);
}
```

#### 3. DependencyConfigurationRegistry

Refactored to use DependencyRulesService instead of hardcoded logic.[4]

Before:

```java
configurations.put("postgresql", DependencyConfiguration.builder("postgresql")
    .requiredProperties(Map.of(
        "spring.datasource.url", "jdbc:postgresql://localhost:5432/${spring.application.name}",
        "spring.datasource.username", "postgres",
        "spring.datasource.password", "postgres"
    ))
    .build());
```

After:

```java
public Optional<DependencyConfiguration> getConfiguration(String dependencyId) {
    return rulesService.getRule(dependencyId)
        .map(rule -> {
            Map<String, String> properties = rule.runtime().properties().stream()
                .collect(Collectors.toMap(p -> p.key(), p -> p.value()));
            return DependencyConfiguration.builder(dependencyId)
                .requiredProperties(properties)
                .build();
        });
}
```

#### 4. PomManipulationService

Refactored to inject Maven dependencies directly from JSON rules.[1]

```java
private String injectFeatureDependencies(String pomContent, ProjectFeatures features, LibraryVersions versions) {
    StringBuilder injections = new StringBuilder();

    if (features.enableMapStruct()) {
        configRegistry.getRule("mapstruct").ifPresent(rule -> {
            injections.append(generateMavenDependenciesXml(rule.build().maven().dependencies()));
        });
    }

    return pomContent.substring(0, lastDependenciesEnd) + injections + pomContent.substring(lastDependenciesEnd);
}
```

#### 5. GradleManipulationService

The same pattern is applied to Gradle:

```java
private String generateGradleDependencies(GradleConfig gradle) {
    StringBuilder deps = new StringBuilder();

    gradle.implementation().forEach(dep ->
        deps.append("    implementation \"").append(dep).append("\"\n")
    );

    gradle.compileOnly().forEach(dep ->
        deps.append("    compileOnly \"").append(dep).append("\"\n")
    );

    return deps.toString();
}
```

#### 6. DockerComposeGeneratorService

New service that generates `docker-compose.yml` entirely from JSON definitions.[2]

```java
public String generateDockerCompose(Set<String> dependencies) {
    List<DockerComposeConfig> services = rules.stream()
        .filter(rule -> rule.infrastructure() != null)
        .filter(rule -> rule.infrastructure().dockerCompose() != null)
        .map(rule -> rule.infrastructure().dockerCompose())
        .collect(Collectors.toList());

    // Generate full YAML
}
```

#### 7. ScaffoldingGeneratorService

New service that generates code files based on rules.[3]

```java
public Map<String, String> generateScaffoldingFiles(Set<String> dependencies, String basePackage, Path projectPath) {
    List<DependencyRule> rules = configRegistry.getRules(new ArrayList<>(dependencies));

    Map<String, String> filesToGenerate = new HashMap<>();

    for (DependencyRule rule : rules) {
        for (ScaffoldingFile file : rule.scaffolding().files()) {
            String resolvedPath = resolvePath(file.path(), basePackage, projectPath);
            String resolvedContent = resolveContent(file.content(), basePackage);
            filesToGenerate.put(resolvedPath, resolvedContent);
        }
    }

    return filesToGenerate;
}
```

## Dependency Priorities

The system supports priorities to guarantee processing order, which is essential for annotation processors.[5]

- Lombok: priority 10 (processed first)
- MapStruct: priority 5 (processed after Lombok)
- Others: priority 0

## Project Generation Flow

```
1. User selects dependencies
        ↓
2. DependencyRulesService loads rules from JSON
        ↓
3. Rules are sorted by priority
        ↓
4. PomManipulationService/GradleManipulationService inject dependencies
        ↓
5. DependencyConfigurationRegistry injects properties into application.yml
        ↓
6. DockerComposeGeneratorService generates docker-compose.yml
        ↓
7. ScaffoldingGeneratorService generates code files
        ↓
8. Complete project is generated
```

## Advantages of the New Architecture

- Zero Hardcoded Logic: All configuration lives in JSON rules.[1]
- Easier Maintenance: Adding new dependencies only requires editing JSON, not Java code.
- Clear Separation of Concerns: Each service has a single responsibility, aligning with clean architecture practices.[6]
- Testability: DependencyRulesService is easy to mock in tests.[5]
- Extensibility: New configuration types can be added without touching existing logic.
- Versioning: JSON configuration is versioned alongside the codebase in the repository.[1]

## Adding a New Dependency

To add support for a new dependency, just add an entry in `dependency-rules.json`:

```json
{
  "id": "new-dependency",
  "category": "TOOL",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "com.example",
          "artifactId": "new-lib",
          "version": "1.0.0"
        }
      ]
    },
    "gradle": {
      "implementation": ["com.example:new-lib:1.0.0"]
    }
  },
  "runtime": {
    "properties": [
      {
        "key": "app.new.enabled",
        "value": "true"
      }
    ]
  },
  "infrastructure": {
    "dockerCompose": null
  },
  "scaffolding": {
    "files": []
  }
}
```

## Supported Dependencies

- lombok
- mapstruct
- postgresql
- mysql
- h2
- mongodb
- redis
- flyway
- security
- web
- actuator
- kafka (with zookeeper)
- zipkin
- graalvm[1]

## Package Structure

```text
com.springcli
├── model
│   └── rules              (JSON data models)
├── service
│   ├── config             (configuration services)
│   ├── DependencyRulesService
│   ├── DockerComposeGeneratorService
│   ├── ScaffoldingGeneratorService
│   ├── PomManipulationService
│   └── GradleManipulationService
└── resources
    └── dependency-rules.json
```

## Summary

The refactor removed roughly 500+ lines of hardcoded configuration and replaced them with a declarative JSON‑based solution that is easier to maintain, test, and extend.[7]

