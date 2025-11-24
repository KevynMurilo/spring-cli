# Spring CLI - Modern Spring Boot Project Generator

A powerful, native CLI tool built with **Spring Shell** and **GraalVM Native Image** for scaffolding Spring Boot projects with custom architectures and best practices.

## 🚀 Features

- **10 Architecture Patterns**: MVC, Clean, Hexagonal, DDD, CQRS, Event-Driven, and more
- **8 Built-in Presets**: REST-API, Microservice, Monolith, Minimal, DDD-API, Enterprise-Layered, CQRS-Service, Event-Driven
- **Interactive Wizard**: Beautiful TUI for project configuration
- **Template Engine**: Pebble Templates for dynamic code generation
- **Smart Dependency Management**: Automatic injection of JWT, Swagger, MapStruct, and more
- **DevOps Ready**: Automatic generation of Dockerfile, docker-compose, CI/CD pipelines, and Kubernetes manifests
- **Fast Native Compilation**: GraalVM Native Image for instant startup
- **Clean Architecture**: Following SOLID principles and best practices

## 📋 Prerequisites

- **Java 21+** (for development and JAR execution)
- **GraalVM 21+** (for native image compilation)
- **Maven 3.8+**
- **Docker** (optional, for containerization)

## 🔧 Installation

### Option 1: Run as JAR (Quick Start)

```bash
# Clone the repository
git clone https://github.com/yourusername/spring-cli.git
cd spring-cli

# Build with Maven
mvn clean package -DskipTests

# Run the CLI
java -jar target/spring-cli-1.0.0.jar
```

### Option 2: Build Native Image (Recommended for Production)

```bash
# Install GraalVM
# Download from: https://www.graalvm.org/downloads/

# Set JAVA_HOME to GraalVM
export JAVA_HOME=/path/to/graalvm
export PATH=$JAVA_HOME/bin:$PATH

# Build native image
mvn clean package -Pnative -DskipTests

# The native executable will be at: target/spring-cli
./target/spring-cli
```

### Option 3: Install Globally

```bash
# After building native image
sudo cp target/spring-cli /usr/local/bin/
spring-cli
```

## 📖 Usage

### Interactive Menu (New! 🎉)

Start with the new interactive menu:

```bash
spring-cli menu
# or shorthand
spring-cli m
```

This shows a beautiful interactive menu with options:
1. 🚀 Generate New Project - Full interactive wizard
2. 📦 Quick Generate - Minimal prompts
3. ⚙️ Configure CLI - Set defaults
4. 📋 List Presets - View templates
5. ℹ️ About - CLI information
6. ❌ Exit

### Classic Interactive Mode

Start the interactive shell:

```bash
spring-cli
```

Then use the `generate` command:

```bash
spring:>generate
```

Follow the interactive wizard to configure your project.

### Quick Generation

Generate a project with minimal prompts:

```bash
spring:>new --artifactId my-app --groupId com.example --architecture CLEAN --output ./projects
```

### Available Commands

| Command | Alias | Description |
|---------|-------|-------------|
| `menu` | `m` | **NEW!** Interactive main menu (recommended) |
| `generate` | - | Interactive project generation wizard |
| `new` | - | Quick project generation with command-line options |
| `list-presets` | `presets` | Show all available presets |
| `show-config` | `config` | Display current user configuration |
| `reset-config` | - | Reset configuration to defaults |
| `clear-cache` | - | Clear metadata cache |
| `refresh-metadata` | - | Refresh metadata from Spring Initializr |
| `delete-preset` | - | Delete a custom preset |
| `version` | - | Show CLI version |
| `info` | - | Show system information |
| `help` | `h` | Show available commands |
| `exit` | - | Exit the CLI |

## 🏗️ Architecture Patterns

### 1. MVC (Model-View-Controller)
Traditional layered architecture with controllers, services, repositories, and models.

```
model/       # Domain entities
dto/         # Data Transfer Objects
mapper/      # Entity-DTO mappers
repository/  # Data access layer
service/     # Business logic
controller/  # REST API endpoints
config/      # Configuration classes
security/    # Security configuration
```

### 2. Feature-Based
Organize code by business features/domains.

```
features/
  ├── demo/
  │   ├── controller/
  │   ├── service/
  │   ├── repository/
  │   └── model/
```

### 3. Clean Architecture
Dependency inversion with domain at the center.

```
domain/
  └── model/
application/
  └── usecase/
infrastructure/
  ├── controller/
  ├── persistence/
  └── config/
```

### 4. Hexagonal (Ports & Adapters)
Isolate business logic from external dependencies.

```
domain/
  └── model/
application/
ports/
  ├── in/
  └── out/
adapters/
  ├── in/
  └── out/
```

### 5. Layered Architecture
Traditional enterprise layered approach.

```
presentation/
  ├── controller/
  └── dto/
business/
  ├── service/
  └── validator/
persistence/
  ├── repository/
  └── entity/
```

### 6. Onion Architecture
Concentric layers with domain at the core.

```
domain/
  ├── model/
  └── services/
application/
  ├── services/
  └── interfaces/
infrastructure/
  ├── persistence/
  ├── web/
  └── config/
```

### 7. DDD (Domain-Driven Design) (Enhanced! ✨)
Strategic and tactical DDD patterns with complete domain modeling.

```
domain/
  ├── entities/         # Domain entities
  ├── aggregates/       # Aggregate roots
  ├── valueobjects/     # Value objects
  ├── events/          # NEW! Domain events
  ├── factories/       # NEW! Aggregate factories
  ├── repositories/     # Repository interfaces
  └── services/        # Domain services
application/
  └── dto/             # Application DTOs
infrastructure/
  ├── persistence/     # JPA implementations
  └── web/            # REST controllers
```

### 8. CQRS (Command Query Responsibility Segregation)
Separate read and write operations.

```
domain/
  └── model/
application/
  ├── commands/
  ├── queries/
  └── handlers/
infrastructure/
  ├── persistence/
  └── web/
```

### 9. Event-Driven
Event-based communication and processing.

```
domain/
  ├── model/
  └── events/
application/
  ├── services/
  └── eventhandlers/
infrastructure/
  ├── messaging/
  ├── persistence/
  └── web/
```

### 10. Vertical-Slice
Feature slices with complete vertical implementations.

```
features/
  └── {feature}/
      ├── domain/
      ├── application/
      └── infrastructure/
```

## 🎯 Built-in Presets

### REST-API
Clean Architecture REST API with JWT, Swagger, and best practices.
- Architecture: Clean
- Dependencies: Web, Data JPA, H2, Validation, Lombok, DevTools
- Features: JWT ✓, Swagger ✓, CORS ✓, Exception Handler ✓, MapStruct ✓, Audit ✓

### Microservice
Hexagonal architecture microservice with cloud-native features.
- Architecture: Hexagonal
- Dependencies: Web, Data JPA, PostgreSQL, Eureka, Config Client, Actuator, Lombok
- Features: All enabled (JWT, Swagger, Docker, K8s, CI/CD)

### Monolith
Traditional MVC monolithic application with web UI.
- Architecture: MVC
- Dependencies: Web, Thymeleaf, Data JPA, MySQL, Security, Validation, Lombok
- Features: Exception Handler ✓, Docker ✓

### Minimal
Minimal Spring Boot application with essential dependencies.
- Architecture: MVC
- Dependencies: Web, Lombok, DevTools
- Features: All disabled (bare minimum)

### DDD-API
Domain-Driven Design API with strategic patterns.
- Architecture: DDD
- Dependencies: Web, Data JPA, PostgreSQL, Validation, Lombok
- Features: JWT ✓, Swagger ✓, CORS ✓, Exception Handler ✓, MapStruct ✓, Audit ✓

### Enterprise-Layered
Traditional enterprise layered architecture.
- Architecture: Layered
- Dependencies: Web, Data JPA, PostgreSQL, Security, Validation, Actuator, Lombok
- Features: Exception Handler ✓, MapStruct ✓, Docker ✓, Audit ✓

### CQRS-Service
CQRS pattern service with command-query separation.
- Architecture: CQRS
- Dependencies: Web, Data JPA, PostgreSQL, Kafka, Lombok, Actuator
- Features: All enabled except Audit

### Event-Driven
Event-driven architecture with messaging.
- Architecture: Event-Driven
- Dependencies: Web, Data JPA, PostgreSQL, Kafka, Cloud Stream, Lombok
- Features: JWT ✓, Swagger ✓, CORS ✓, Exception Handler ✓, Docker ✓, K8s ✓, CI/CD ✓

## 🔥 Project Features

### JWT Authentication (Enhanced! ✨)
Automatic generation of complete authentication system:
- `JwtService` - Token generation and validation
- `JwtAuthenticationFilter` - Request interceptor
- `JwtAuthenticationEntryPoint` - Unauthorized handler
- `SecurityConfig` - Spring Security configuration (stateless)
- `UserDetailsServiceImpl` - User loading service
- **`AuthController`** - Authentication endpoints:
  - `POST /api/auth/login` - Login with credentials
  - `GET /api/auth/validate` - Validate token
- **`LoginRequest`** - Login request DTO
- **`AuthResponse`** - Auth response with token

Example usage:
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}'

# Response
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "username": "admin"
}
```

### Swagger/OpenAPI
- Complete OpenAPI 3.0 configuration
- JWT bearer authentication integration
- Accessible at: `http://localhost:8080/swagger-ui.html`

### Global Exception Handler
- Centralized exception handling
- Standard API response format
- Pagination support
- Common exceptions (ResourceNotFoundException, BadRequestException)

### Docker Support
- Multi-stage Dockerfile with optimized builds
- docker-compose.yml with database integration
- Non-root user configuration
- Health checks

### Kubernetes Manifests
- Deployment with ConfigMap
- Resource limits
- Liveness and readiness probes

### CI/CD Pipeline
- GitHub Actions workflow
- Build, test, and security scan stages
- Docker image push to DockerHub

### JPA Auditing
- Automatic `createdAt` and `updatedAt` timestamps
- Base entity with common fields

## 🛠️ Configuration

### User Configuration File

Location: `~/.springclirc.json`

```json
{
  "defaultGroupId": "com.example",
  "defaultJavaVersion": "JAVA_21",
  "defaultPackaging": "JAR",
  "defaultArchitecture": "CLEAN",
  "defaultOutputDir": ".",
  "autoOpenIde": false,
  "preferredIde": "idea",
  "useApplicationYml": true,
  "generateReadme": true,
  "generateGitignore": true
}
```

### Cache

Metadata is cached for 24 hours at: `~/.spring-cli/metadata-cache.json`

Clear cache:
```bash
spring:>clear-cache
```

### Custom Presets

Save custom presets for reuse:

Location: `~/.spring-cli/presets/`

Presets are saved automatically when you generate a project and choose to save the configuration.

## 📁 Project Structure

```
spring-cli/
├── src/main/java/com/springcli/
│   ├── command/              # Spring Shell commands
│   │   ├── GenerateCommand.java
│   │   └── UtilityCommands.java
│   │
│   ├── service/              # Business logic
│   │   ├── ProjectGeneratorService.java
│   │   ├── TemplateService.java
│   │   ├── PresetService.java
│   │   ├── MetadataService.java
│   │   ├── ConfigService.java
│   │   ├── CacheService.java
│   │   └── PomManipulationService.java
│   │
│   ├── model/                # Domain models (Records)
│   │   ├── Architecture.java
│   │   ├── ProjectConfig.java
│   │   ├── ProjectFeatures.java
│   │   ├── Preset.java
│   │   ├── SpringMetadata.java
│   │   └── UserConfig.java
│   │
│   ├── client/               # External API client
│   │   └── SpringInitializrClient.java
│   │
│   └── infra/                # Infrastructure
│       ├── console/
│       │   └── ConsoleService.java
│       └── filesystem/
│           └── FileSystemService.java
│
├── src/main/resources/
│   ├── templates/            # Pebble templates
│   │   ├── java/
│   │   │   ├── mvc/
│   │   │   ├── clean/
│   │   │   ├── hexagonal/
│   │   │   └── common/
│   │   ├── config/
│   │   └── ops/
│   │
│   └── META-INF/native-image/
│       ├── reflect-config.json
│       └── resource-config.json
│
└── pom.xml
```

## 🧪 Examples

### Example 1: REST API with Clean Architecture

```bash
spring:>generate
# Select "REST-API" preset
# Enter artifact ID: my-api
# Enter group ID: com.mycompany
# Output directory: ./projects
```

Generates:
- Clean Architecture structure
- JWT authentication
- Swagger documentation
- Exception handling
- H2 database configuration
- Docker support

### Example 2: Microservice with Hexagonal Architecture

```bash
spring:>generate
# Select "Microservice" preset
# Customize dependencies if needed
```

Generates:
- Hexagonal Architecture
- PostgreSQL configuration
- Eureka client
- Kubernetes manifests
- CI/CD pipeline
- Complete Docker setup

### Example 3: Quick Start

```bash
spring:>new --artifactId quick-app --groupId com.example --architecture MVC --output .
```

Generates a minimal MVC application instantly.

## 🎨 Template System

Templates use **Pebble** syntax for dynamic generation.

### Example Template (Controller.peb)

```java
package {{ packageName }}.controller;

import org.springframework.web.bind.annotation.*;
{% if enableSwagger %}
import io.swagger.v3.oas.annotations.Operation;
{% endif %}
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/{{ entityName | lower }}")
@RequiredArgsConstructor
public class {{ entityName }}Controller {

    private final {{ entityName }}Service service;

    {% if enableSwagger %}
    @Operation(summary = "Get all {{ entityName | lower }}s")
    {% endif %}
    @GetMapping
    public List<{{ entityName }}> getAll() {
        return service.findAll();
    }
}
```

### Custom Templates

Add your own templates in:
- `src/main/resources/templates/java/{architecture}/`
- `src/main/resources/templates/config/`
- `src/main/resources/templates/ops/`

## 🚀 Performance

### Startup Time Comparison

| Execution Mode | Startup Time | Memory Usage |
|----------------|-------------|--------------|
| JAR (JVM) | ~2-3 seconds | ~150 MB |
| Native Image | ~0.1 seconds | ~50 MB |

Native image is **20-30x faster** to start!

## 🔒 Security Best Practices

Generated projects include:
- JWT with secure secret key placeholders
- CORS configuration
- Spring Security with stateless session management
- Input validation with Bean Validation
- Global exception handling
- Non-root Docker user

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Follow Clean Architecture principles
4. Write code in English
5. No comments (self-documenting code)
6. Apply SOLID principles
7. Test your changes
8. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- **Spring Boot** - Application framework
- **Spring Shell** - Interactive CLI framework
- **Pebble** - Template engine
- **GraalVM** - Native image compilation
- **Spring Initializr** - Project metadata API

## 📧 Support

For issues and questions:
- GitHub Issues: [https://github.com/yourusername/spring-cli/issues](https://github.com/yourusername/spring-cli/issues)

## 🔄 Roadmap

- [ ] Add Gradle support
- [ ] More architecture patterns (Microkernel, Space-Based)
- [ ] Integration with more template engines
- [ ] GUI version
- [ ] Plugin system for custom generators
- [ ] Multi-language support (i18n)
- [ ] Cloud provider integrations (AWS, Azure, GCP)

## 📊 Version History

### v1.0.1 (2025-01-24) - Latest ⭐
- ✨ **NEW: Interactive Menu System** - Beautiful menu-driven interface
- ✨ **Enhanced JWT Authentication** - Complete auth API with login/validate endpoints
- ✨ **DTO & Mapper Templates** - Added for all architectures
- ✨ **DDD Enhancements** - Domain Events and Factory patterns
- ✨ **Visual Improvements** - Colorful, intuitive CLI interface
- 🎯 **64 Production-Ready Templates** (was 57)
- 🎨 Enhanced ConsoleService with better styling
- 📚 Complete documentation update

### v1.0.0 (2025-01-24)
- Initial release
- 10 architecture patterns
- 8 built-in presets
- GraalVM Native Image support
- Complete template system
- Interactive wizard
- Docker and Kubernetes support
- CI/CD pipeline generation

---

**Made with ❤️ using Clean Architecture and SOLID principles**
