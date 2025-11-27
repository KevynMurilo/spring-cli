# Contribution Guide - Spring CLI

Thank you for contributing to Spring CLI! This guide explains how to add new dependencies and features to the system.

## Index

1. [Architecture Overview](#architecture-overview)
2. [How to Add a New Dependency](#how-to-add-a-new-dependency)
3. [JSON Schema](#json-schema)
4. [Business Rules](#business-rules)
5. [Practical Examples](#practical-examples)
6. [Testing Your Changes](#testing-your-changes)
7. [Best Practices](#best-practices)

## Architecture Overview

Spring CLI uses a **100% declarative JSON-based system** to manage dependencies. There is no hardcoded if/else logic in the services.[1]

### Configuration Flow

```
dependency-rules.json (single source of truth)
        ↓
DependencyRulesService (loads and caches rules)
        ↓
┌────────────────────────────────────────┐
│ Services that READ from JSON:         │
│ • DependencyConfigurationRegistry      │ → application.yml
│ • PomManipulationService               │ → pom.xml
│ • GradleManipulationService            │ → build.gradle
│ • DockerComposeGeneratorService        │ → docker-compose.yml
│ • ScaffoldingGeneratorService          │ → Java code
└────────────────────────────────────────┘
```

Important: Services MUST NEVER contain dependency-specific logic. Everything comes from JSON.

## How to Add a New Dependency

### Step 1: Edit `dependency-rules.json`

Add your rule to `src/main/resources/dependency-rules.json`:

```json
{
  "id": "your-dependency",
  "category": "TOOL",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [ ... ],
      "plugins": [ ... ],
      "exclusions": [ ... ]
    },
    "gradle": {
      "implementation": [ ... ],
      "compileOnly": [ ... ],
      "runtimeOnly": [ ... ],
      "annotationProcessor": [ ... ],
      "compilerOptions": [ ... ]
    }
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

### Step 2: If It Is a Feature, Map the ID

If your dependency is activated by a feature (such as JWT or Swagger), add the mapping in:

`PomManipulationService.java` and `GradleManipulationService.java`:

```java
private List<String> getActiveFeaturesAsDependencyIds(ProjectFeatures features) {
    List<String> dependencies = new ArrayList<>();

    if (features.enableJwt()) {
        dependencies.add("jwt");
    }
    if (features.enableSwagger()) {
        dependencies.add("swagger");
    }
    // ADD HERE:
    if (features.enableYourNewDependency()) {
        dependencies.add("your-dependency");
    }

    return dependencies;
}
```

### Step 3: Compile and Test

```bash
mvn clean compile
mvn test
```

## JSON Schema

### Full Structure

```json
{
  "id": "string (REQUIRED - unique identifier)",
  "category": "string (REQUIRED - DATA, SECURITY, TOOL, IO, OBSERVABILITY)",
  "priority": "integer (REQUIRED - 0 to 10, where 10 = highest priority)",

  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "string (REQUIRED)",
          "artifactId": "string (REQUIRED)",
          "version": "string (OPTIONAL - omit if managed by BOM)",
          "scope": "string (OPTIONAL - compile, runtime, provided, test)"
        }
      ],
      "plugins": [
        {
          "groupId": "string (REQUIRED)",
          "artifactId": "string (REQUIRED)",
          "executionGoal": "string (OPTIONAL - compile, test, etc.)"
        }
      ],
      "exclusions": [
        {
          "groupId": "string (REQUIRED)",
          "artifactId": "string (REQUIRED)"
        }
      ]
    },
    "gradle": {
      "implementation": ["string - format: groupId:artifactId:version"],
      "compileOnly": ["string"],
      "runtimeOnly": ["string"],
      "annotationProcessor": ["string"],
      "compilerOptions": ["string - compiler flags such as -Amapstruct..."],
      "plugins": ["string - Gradle plugin ID"]
    }
  },

  "runtime": {
    "properties": [
      {
        "key": "string (REQUIRED - Spring property key)",
        "value": "string (REQUIRED - default value)",
        "comment": "string (OPTIONAL - explanatory comment)"
      }
    ]
  },

  "infrastructure": {
    "dockerCompose": {
      "serviceName": "string (REQUIRED - service name in docker-compose)",
      "image": "string (REQUIRED - Docker image with tag)",
      "ports": ["string - format: host:container"],
      "environment": {
        "KEY": "value"
      },
      "volumes": ["string - format: volume:mountpoint"],
      "depends_on": ["string - other service name"],
      "healthcheck": {
        "test": ["CMD", "command", "args"],
        "interval": "string (e.g. 10s)",
        "timeout": "string (e.g. 5s)",
        "retries": "integer"
      }
    }
  },

  "scaffolding": {
    "files": [
      {
        "path": "string (REQUIRED - file path with {{basePackage}})",
        "content": "string (REQUIRED - file content with {{basePackage}} support)"
      }
    ]
  }
}
```

### Null Fields vs Empty Arrays

- Use `null` when the entire section is not applicable (for example, Docker Compose for Lombok).
- Use `[]` (empty array) when the section exists but has no items (for example, Maven plugins for PostgreSQL).

## Business Rules

### 1. Priorities

Dependencies with annotation processors must have specific priorities:

| Dependency | Priority | Reason |
|-----------|----------|--------|
| Lombok    | 10       | Must run first |
| MapStruct | 5        | Depends on Lombok |
| Others    | 0        | Default |

### 2. Databases

Connection URLs in Docker:

For databases, always use the Docker service name as hostname:

```json
{
  "key": "spring.datasource.url",
  "value": "jdbc:postgresql://postgres:5432/mydb"
}
```

Do NOT use `localhost` – it will break when running in Docker.[2]

### 3. Infrastructure

Kafka: Always create TWO services in JSON:

- `kafka-zookeeper` (separate id)
- `kafka` (with `depends_on: ["zookeeper"]`)

Healthchecks: Always add healthchecks for infrastructure containers.

### 4. Scaffolding

Template variables:

- `{{basePackage}}`: Replaced by the base package (for example, `com.example.app`)

SecurityConfig: For Spring Security, always generate `SecurityConfig.java` with `permitAll()` to avoid locking out the developer.[3]

Flyway: Create folder `src/main/resources/db/migration` with a `.gitkeep` or an initial migration.

### 5. Maven Scopes

| Scope      | When to Use |
|-----------|-------------|
| `compile` (default) | Available at compile and runtime |
| `provided`          | Provided by the container (for example, Lombok) |
| `runtime`           | Only needed at runtime (for example, JDBC drivers) |
| `test`              | Test-only |

### 6. Gradle Configurations

| Configuration      | Maven Equivalent | When to Use |
|--------------------|------------------|------------|
| `implementation`   | `compile`        | Regular dependency |
| `compileOnly`      | `provided`       | Available at compile but not runtime |
| `runtimeOnly`      | `runtime`        | Available only at runtime |
| `annotationProcessor` | N/A           | Annotation processors (Lombok, MapStruct) |

## Practical Examples

### Example 1: Simple Dependency (Library)

```json
{
  "id": "commons-lang3",
  "category": "TOOL",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "org.apache.commons",
          "artifactId": "commons-lang3",
          "version": "3.14.0"
        }
      ],
      "plugins": [],
      "exclusions": []
    },
    "gradle": {
      "implementation": ["org.apache.commons:commons-lang3:3.14.0"],
      "compileOnly": [],
      "runtimeOnly": [],
      "annotationProcessor": [],
      "compilerOptions": []
    }
  },
  "runtime": {
    "properties": []
  },
  "infrastructure": {
    "dockerCompose": null
  },
  "scaffolding": {
    "files": []
  }
}
```

### Example 2: Database with Docker

```json
{
  "id": "mariadb",
  "category": "DATA",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "org.springframework.boot",
          "artifactId": "spring-boot-starter-data-jpa"
        },
        {
          "groupId": "org.mariadb.jdbc",
          "artifactId": "mariadb-java-client",
          "scope": "runtime"
        }
      ],
      "plugins": [],
      "exclusions": []
    },
    "gradle": {
      "implementation": ["org.springframework.boot:spring-boot-starter-data-jpa"],
      "compileOnly": [],
      "runtimeOnly": ["org.mariadb.jdbc:mariadb-java-client"],
      "annotationProcessor": [],
      "compilerOptions": []
    }
  },
  "runtime": {
    "properties": [
      {
        "key": "spring.datasource.url",
        "value": "jdbc:mariadb://mariadb:3306/mydb"
      },
      {
        "key": "spring.datasource.username",
        "value": "root"
      },
      {
        "key": "spring.datasource.password",
        "value": "root"
      },
      {
        "key": "spring.datasource.driver-class-name",
        "value": "org.mariadb.jdbc.Driver"
      },
      {
        "key": "spring.jpa.database-platform",
        "value": "org.hibernate.dialect.MariaDBDialect"
      }
    ]
  },
  "infrastructure": {
    "dockerCompose": {
      "serviceName": "mariadb",
      "image": "mariadb:11.2",
      "ports": ["3306:3306"],
      "environment": {
        "MARIADB_DATABASE": "mydb",
        "MARIADB_ROOT_PASSWORD": "root"
      },
      "volumes": ["mariadb_data:/var/lib/mysql"],
      "healthcheck": {
        "test": ["CMD", "healthcheck.sh", "--connect", "--innodb_initialized"],
        "interval": "10s",
        "timeout": "5s",
        "retries": 5
      }
    }
  },
  "scaffolding": {
    "files": [
      {
        "path": "src/main/java/{{basePackage}}/entity/package-info.java",
        "content": "package {{basePackage}}.entity;"
      }
    ]
  }
}
```

### Example 3: Feature with Scaffolding

```json
{
  "id": "graphql",
  "category": "IO",
  "priority": 0,
  "build": {
    "maven": {
      "dependencies": [
        {
          "groupId": "org.springframework.boot",
          "artifactId": "spring-boot-starter-graphql"
        }
      ],
      "plugins": [],
      "exclusions": []
    },
    "gradle": {
      "implementation": ["org.springframework.boot:spring-boot-starter-graphql"],
      "compileOnly": [],
      "runtimeOnly": [],
      "annotationProcessor": [],
      "compilerOptions": []
    }
  },
  "runtime": {
    "properties": [
      {
        "key": "spring.graphql.graphiql.enabled",
        "value": "true"
      },
      {
        "key": "spring.graphql.graphiql.path",
        "value": "/graphiql"
      }
    ]
  },
  "infrastructure": {
    "dockerCompose": null
  },
  "scaffolding": {
    "files": [
      {
        "path": "src/main/resources/graphql/schema.graphqls",
        "content": "type Query {\n    hello: String\n}\n"
      },
      {
        "path": "src/main/java/{{basePackage}}/graphql/QueryResolver.java",
        "content": "package {{basePackage}}.graphql;\n\nimport org.springframework.graphql.data.method.annotation.QueryMapping;\nimport org.springframework.stereotype.Controller;\n\n@Controller\npublic class QueryResolver {\n\n    @QueryMapping\n    public String hello() {\n        return \"Hello from GraphQL!\";\n    }\n}\n"
      }
    ]
  }
}
```

## Testing Your Changes

### 1. JSON Validation

Before committing, validate the JSON:

```bash
cat src/main/resources/dependency-rules.json | jq . > /dev/null
```

If there is a syntax error, `jq` will point to the exact line.

### 2. Compilation

```bash
mvn clean compile
```

### 3. Unit Tests

```bash
mvn test
```

### 4. Integration Test

Generate a real project using your new dependency:

```bash
mvn clean package -DskipTests
./target/spring-cli
```

In the interactive CLI, select your new dependency and generate a project.[4][1]

### 5. Checks

After generating the project:

1. Build: Does pom.xml/build.gradle contain the correct dependencies?
2. Runtime: Does application.yml contain the correct properties?
3. Infrastructure: Was docker-compose.yml generated (if applicable)?
4. Scaffolding: Were Java files created correctly?
5. Compile: Does the generated project compile without errors?

```bash
cd generated-project
mvn clean compile  # or ./gradlew build
```

## Best Practices

### ✅ DO

1. Always use explicit versions for libraries not in the Spring Boot BOM.[5]
2. Add healthchecks for all Docker containers.
3. Use Docker service names in connection URLs.
4. Document properties using the `comment` field.
5. Test with both Maven AND Gradle to ensure both work.[5]
6. Use `{{basePackage}}` templates in scaffolding.
7. Order properties logically (URL first, credentials next, advanced settings last).
8. Create `package-info.java` for new packages in scaffolding.

### ❌ DO NOT

1. Do not add dependency-specific logic to Java services – everything must come from JSON.
2. Do not use `localhost` in database properties.
3. Do not omit the `category` field – it may be used for future grouping.
4. Do not create circular dependencies in Docker Compose.
5. Do not use SNAPSHOT versions – only stable releases.
6. Do not add unnecessary dependencies – keep it minimal.
7. Do not break compatibility with older Spring Boot versions without documenting it.[3]

### Performance Tips

1. Priorities: Use only when truly necessary (annotation processors).
2. Exclusions: Use them to avoid version conflicts.[3]
3. Scopes: Use `provided` or `runtime` whenever possible to reduce the compile classpath.

### Documentation

When adding a significant dependency:

1. Update `README.md` with the new dependency in the list.
2. If it is a complex feature, add an example in `ARCHITECTURE.md`.
3. If the JSON schema changes, update this `CONTRIBUTING.md`.

## Commit Structure

When committing new dependencies:

```text
feat(deps): add MariaDB support

- Add MariaDB dependency rule to dependency-rules.json
- Include Docker Compose configuration with healthcheck
- Add connection properties pointing to Docker service
- Generate entity package scaffold

Closes #123
```

## FAQ

### Q: Do I need to add Java code when I add a dependency?

A: Only if the dependency requires scaffolding (initial code). Otherwise, JSON alone is enough.

### Q: How do I add support for a complex Maven plugin?

A: Use the `executionGoal` field in `maven.plugins`. For complex XML configuration, consider creating a template.

### Q: Can I have multiple versions of the same dependency?

A: Not directly. The `id` must be unique. If you need variants, use different IDs (for example, `postgresql-14`, `postgresql-15`).

### Q: How do I test only my dependency without generating a full project?

A: Create a unit test in `DependencyRulesServiceTest.java` that validates your rule.

### Q: What if my dependency conflicts with another?

A: Use the `exclusions` field in Maven or exclude mechanisms in Gradle to resolve conflicts.[3]

## Support

Questions? Open an issue:  
https://github.com/spring-cli/issues

