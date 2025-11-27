# Spring CLI - Project Generator

A modern, JSON-driven Spring Boot project generator with interactive CLI and web GUI.

## Features

- 🎯 **JSON-Based Configuration**: All dependency rules in one declarative file
- 🚀 **23 Dependencies Supported**: Complete coverage for modern Spring Boot apps
- 🐳 **Docker Ready**: Auto-generated docker-compose.yml with healthchecks
- 📦 **Maven & Gradle**: Full support for both build tools
- 🎨 **Scaffolding**: Generates configuration classes, security, and more
- 🔄 **Auto Updates**: Checks for new versions on startup
- 🌐 **Web GUI**: Browser-based project generation
- ⌨️ **Interactive CLI**: Terminal-based project wizard

## Supported Dependencies

### Data Access (8)
- PostgreSQL, MySQL, H2, MongoDB, Redis
- Flyway, Elasticsearch, JPA

### Security (3)
- Spring Security, JWT, CORS

### I/O & Messaging (4)
- Spring Web, Kafka, RabbitMQ, GraphQL

### Tools (5)
- Lombok, MapStruct, Swagger/OpenAPI, GraalVM

### Observability (3)
- Actuator, Zipkin, Prometheus

## Quick Start

### Build

```bash
mvn clean package -DskipTests
```

### Run CLI

```bash
./target/spring-cli
```

### Run Web GUI

```bash
mvn spring-boot:run
```

Then open: http://localhost:8080

## Usage

### CLI Mode

1. Start the CLI
2. Select dependencies interactively
3. Choose architecture (Layered, Hexagonal, DDD)
4. Configure project details
5. Generate!

### Web GUI Mode

1. Access http://localhost:8080
2. Fill the form with project details
3. Select dependencies with checkboxes
4. Click "Generate Project"
5. Download ZIP file

## Architecture

The system is 100% JSON-driven:

```
dependency-rules.json (1,300+ lines)
        ↓
DependencyRulesService (loads & caches)
        ↓
┌─────────────────────────────────────┐
│ Services (read from JSON):          │
│ • DependencyConfigurationRegistry   │ → application.yml
│ • PomManipulationService            │ → pom.xml
│ • GradleManipulationService         │ → build.gradle
│ • DockerComposeGeneratorService     │ → docker-compose.yml
│ • ScaffoldingGeneratorService       │ → Java code
└─────────────────────────────────────┘
```

No hardcoded if/else logic. Everything is declarative.

## Adding New Dependencies

Edit `src/main/resources/dependency-rules.json`:

```json
{
  "id": "my-dependency",
  "category": "TOOL",
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

## Project Structure

```
spring-cli/
├── src/main/java/com/springcli/
│   ├── model/rules/          # JSON model classes
│   ├── service/              # Business logic
│   │   ├── DependencyRulesService
│   │   ├── DockerComposeGeneratorService
│   │   ├── ScaffoldingGeneratorService
│   │   ├── PomManipulationService
│   │   ├── GradleManipulationService
│   │   └── UpdateCheckService
│   ├── command/              # CLI commands
│   └── web/                  # Web controllers
├── src/main/resources/
│   └── dependency-rules.json # Configuration source
└── src/test/java/            # Comprehensive tests
```

## Testing

```bash
mvn test
```

Test coverage includes:
- JSON loading and validation
- Priority ordering
- Docker Compose generation
- Scaffolding with template substitution
- Update checking

## Configuration

### Update Check

Edit `UpdateCheckService.java`:

```java
private static final String GITHUB_API_URL = "https://api.github.com/repos/YOUR_REPO/releases/latest";
```

### Default Settings

All defaults are in `dependency-rules.json`. No code changes needed.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add your dependency to `dependency-rules.json`
4. Write tests
5. Submit a pull request

## Requirements

- Java 17+
- Maven 3.6+ or Gradle 7+
- Docker (optional, for infrastructure)

## License

Apache License 2.0

## Author

Kevyn Murilo

## Links

- GitHub: https://github.com/KevynMurilo/spring-cli
- Issues: https://github.com/KevynMurilo/spring-cli/issues
