# 🚀 Spring CLI Generator

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=spring&logoColor=white)
![Java](https://img.shields.io/badge/Java-21+-orange?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Native Image](https://img.shields.io/badge/GraalVM-Native%20Image-blueviolet?logo=oracle&logoColor=white)

**A modern, interactive CLI tool for generating production-ready Spring Boot projects with industry best practices**

[Features](#-features) • [Installation](#-installation) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Usage](#-usage)
- [Architecture Patterns](#-architecture-patterns)
- [Features & Technologies](#-features--technologies)
- [Presets](#-presets)
- [Configuration](#-configuration)
- [Building from Source](#-building-from-source)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview

Spring CLI Generator is a powerful command-line tool that streamlines Spring Boot project creation with:

- **🎨 Multiple Architecture Patterns** - Choose from MVC, Clean, Hexagonal, DDD, CQRS, and more
- **⚡ Interactive CLI** - Beautiful, intuitive interface with color-coded output
- **📦 Smart Presets** - Built-in and custom presets for rapid project scaffolding
- **🔧 Dynamic Dependencies** - Automatic version resolution based on Spring Boot version
- **🐳 DevOps Ready** - Pre-configured Docker, Kubernetes, and CI/CD files
- **🚀 GraalVM Native** - Compiled to native executable for instant startup

---

## ✨ Features

### Core Capabilities

- ✅ **10 Architecture Patterns** - From simple MVC to advanced CQRS
- ✅ **Multi Build Tool Support** - Maven and Gradle with Kotlin DSL
- ✅ **Dynamic Version Management** - Spring Boot BOM integration
- ✅ **JWT Authentication** - Pre-configured Spring Security with JWT
- ✅ **API Documentation** - Automatic Swagger/OpenAPI integration
- ✅ **Database Support** - PostgreSQL, MySQL, H2, MongoDB, and more
- ✅ **Docker & Kubernetes** - Production-ready containerization
- ✅ **CI/CD Templates** - GitHub Actions, GitLab CI, and Jenkins
- ✅ **MapStruct Integration** - Type-safe object mapping
- ✅ **Validation & Error Handling** - Global exception handlers
- ✅ **CORS Configuration** - Secure cross-origin setup
- ✅ **Database Auditing** - Automatic entity auditing

### Developer Experience

- 🎯 **Interactive Menus** - Navigate with arrow keys, clear visual feedback
- 🎨 **Color-Coded Output** - Distinguishable states and messages
- 📊 **Progress Indicators** - Real-time feedback during generation
- 💾 **Custom Presets** - Save and reuse your favorite configurations
- ⚙️ **Persistent Configuration** - Remember your preferences
- 🔄 **Live Metadata** - Always up-to-date with Spring Initializr

---

## 📥 Installation

### Prerequisites

- **Java 21+** (OpenJDK or Oracle JDK)
- **Maven 3.9+** or **Gradle 8.5+** (for building from source)
- **Git** (optional, for cloning repository)

### Option 1: Download Pre-built Binary (Recommended)

Download the latest native executable from the [Releases](https://github.com/KevynMurilo/spring-cli/releases) page:

```bash
# Linux/macOS
chmod +x spring-cli
./spring-cli

# Windows
spring-cli.exe
```

### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/KevynMurilo/spring-cli.git
cd spring-cli

# Build with Maven
mvn clean package -DskipTests

# Run
java -jar target/spring-cli.jar

# Or build native image (requires GraalVM)
mvn -Pnative native:compile
./target/spring-cli
```

---

## 🚀 Quick Start

### Interactive Mode (Recommended)

Simply run the CLI and navigate through the menus:

```bash
./spring-cli menu
```

### Command-Line Mode

Generate a project directly from the command line:

```bash
# Basic generation
./spring-cli new my-api

# With architecture
./spring-cli new my-api --architecture HEXAGONAL

# With preset
./spring-cli new my-api --preset rest-api

# Full customization
./spring-cli new my-api \
  --group com.example \
  --architecture CLEAN \
  --java 21 \
  --output ./projects
```

---

## 📖 Usage

### Main Menu Options

```
🚀 Generate New Project - Create a complete Spring Boot project
📦 Quick Generate       - Fast project generation (interactive)
⭐ Manage Presets       - Create, edit, or delete custom presets
📋 List Presets         - View available project templates
⚙️  Configure CLI        - Set default preferences (interactive)
🛠️  Utilities            - Clear cache, refresh metadata, system info
ℹ️  About                - Information about Spring CLI
❌ Exit                  - Close the application
```

### Generating a Project

1. **Select Architecture Pattern** - Choose from 10 different patterns
2. **Configure Project Details** - Set groupId, artifactId, name, etc.
3. **Select Spring Boot Version** - Automatic compatibility checking
4. **Choose Dependencies** - Browse by category or search
5. **Enable Features** - JWT, Swagger, Docker, K8s, CI/CD, etc.
6. **Generate** - Creates project with all files and configurations

### Using Presets

Presets allow you to save common configurations:

```bash
# List available presets
./spring-cli list-presets

# Create custom preset
./spring-cli menu → Manage Presets → Create New Preset

# Use preset in generation
./spring-cli new my-api --preset microservice-template
```

**Built-in Presets:**
- `rest-api` - RESTful API with JWT and Swagger
- `microservice` - Microservice with Spring Cloud
- `web-app` - Full-stack web application
- `batch-processor` - Spring Batch application
- `reactive-api` - WebFlux reactive application

---

## 🏗️ Architecture Patterns

### Available Architectures

| Architecture | Description | Best For |
|-------------|-------------|----------|
| **MVC** | Model-View-Controller | Simple web applications |
| **LAYERED** | Traditional layered architecture | Standard business applications |
| **CLEAN** | Clean Architecture (Uncle Bob) | Testable, maintainable systems |
| **HEXAGONAL** | Ports & Adapters | Domain-driven, decoupled applications |
| **DDD** | Domain-Driven Design | Complex business domains |
| **CQRS** | Command Query Responsibility Segregation | Read/write separation needed |
| **FEATURE_DRIVEN** | Features as first-class modules | Feature-based organization |
| **MODULAR** | Modular monolith | Large monolithic applications |
| **SCREAMING** | Screaming Architecture | Use-case driven development |
| **SERVICE_ORIENTED** | SOA principles | Service-based decomposition |

### Package Structure Examples

<details>
<summary><b>Clean Architecture</b></summary>

```
src/main/java/com/example/myapi/
├── domain/
│   ├── model/          # Entities
│   └── exception/      # Domain exceptions
├── application/
│   ├── usecase/        # Use cases
│   ├── port/
│   │   ├── in/         # Input ports
│   │   └── out/        # Output ports
│   └── dto/            # Data Transfer Objects
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/    # REST controllers
    │   └── out/
    │       └── persistence/  # Repository implementations
    ├── config/         # Spring configuration
    └── security/       # Security configuration
```

</details>

<details>
<summary><b>Hexagonal Architecture</b></summary>

```
src/main/java/com/example/myapi/
├── domain/
│   └── model/          # Domain entities
├── application/
│   ├── service/        # Application services
│   ├── port/
│   │   └── out/        # Output ports (interfaces)
│   └── dto/            # DTOs
└── adapter/
    ├── in/
    │   └── web/        # REST controllers
    ├── out/
    │   └── persistence/  # Repository implementations
    ├── config/         # Configuration
    └── security/       # Security
```

</details>

---

## 🔧 Features & Technologies

### Security

- **Spring Security** - Industry-standard security
- **JWT Authentication** - Stateless authentication with JJWT
- **Password Encryption** - BCrypt hashing
- **CORS Configuration** - Secure cross-origin setup
- **Method Security** - Annotation-based authorization

### API Documentation

- **SpringDoc OpenAPI 3** - Automatic API documentation
- **Swagger UI** - Interactive API explorer
- **Customizable** - Annotations for detailed documentation

### Data & Persistence

- **Spring Data JPA** - Simplified data access
- **Hibernate** - ORM framework
- **Database Support:**
  - PostgreSQL
  - MySQL
  - H2 (development)
  - MongoDB (NoSQL)
  - SQL Server
  - Oracle

### Validation & Mapping

- **Bean Validation** - JSR-303/380 validation
- **MapStruct** - Compile-time type-safe mapping
- **Lombok** - Boilerplate code reduction

### DevOps & Deployment

- **Docker** - Multi-stage optimized Dockerfiles
- **Docker Compose** - Local development environment
- **Kubernetes** - Deployment, service, ingress manifests
- **CI/CD Templates:**
  - GitHub Actions
  - GitLab CI
  - Jenkins
  - CircleCI

### Observability

- **Spring Actuator** - Health checks and metrics
- **Prometheus** - Metrics collection
- **Grafana** - Metrics visualization
- **Logging** - Logback with structured logging

---

## 📦 Presets

### Managing Presets

Create custom presets to save your favorite configurations:

```bash
# Interactive preset creation
./spring-cli menu → Manage Presets → Create New Preset

# Or via command
./spring-cli preset create
```

**Preset Configuration:**
- Architecture pattern
- Java version
- Dependencies
- Features (JWT, Swagger, Docker, etc.)
- Build tool preference

### Sharing Presets

Presets are stored in `~/.spring-cli/presets/` as JSON files. You can:

- Share presets with your team
- Version control preset configurations
- Import/export preset collections

---

## ⚙️ Configuration

### CLI Configuration

Configure default values to speed up project generation:

```bash
./spring-cli menu → Configure CLI
```

**Configurable Options:**
- Default group ID
- Default Java version
- Default packaging (JAR/WAR)
- Default architecture
- Output directory
- Auto-open IDE
- Preferred IDE
- File format preferences

### Configuration File Location

```
~/.spring-cli/
├── config.json         # CLI configuration
├── presets/            # Custom presets
└── cache/              # Metadata cache
```

### Refreshing Metadata

Keep Spring Initializr data up-to-date:

```bash
./spring-cli refresh-metadata
```

---

## 🔨 Building from Source

### Requirements

- JDK 21+
- Maven 3.9+ or Gradle 8.5+
- GraalVM 21+ (for native image)

### Build Steps

```bash
# Clone repository
git clone https://github.com/KevynMurilo/spring-cli.git
cd spring-cli

# Build JAR
mvn clean package

# Run tests
mvn test

# Build native image (requires GraalVM)
mvn -Pnative native:compile

# Run native executable
./target/spring-cli
```

### Development Mode

```bash
# Run in development with auto-reload
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 📚 Documentation

Detailed documentation is available in the `docs/` directory:

- **[Architectures Guide](docs/ARCHITECTURES.md)** - Deep dive into each architecture pattern
- **[Build Guide](docs/BUILD.md)** - Building and customizing the CLI
- **[Preset Management](docs/PRESET_MANAGEMENT.md)** - Advanced preset usage
- **[Contributing Guide](docs/CONTRIBUTING.md)** - How to contribute

### Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Initializr](https://start.spring.io/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)

---

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guide](docs/CONTRIBUTING.md) for details on:

- Code of conduct
- Development process
- Submitting pull requests
- Coding standards
- Testing guidelines

### Quick Contribution Steps

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

**Copyright © 2024 Kevyn Murilo**

While this project is open source and freely available, the original authorship and copyright belong to Kevyn Murilo. You are free to use, modify, and distribute this software under the terms of the MIT License.

---

## 🙏 Acknowledgments

- **Spring Team** - For the amazing Spring Framework and Spring Boot
- **Spring Initializr** - For the metadata API and inspiration
- **JLine** - For the beautiful terminal UI components
- **GraalVM Team** - For native compilation support
- **Contributors** - Everyone who has contributed to this project

---

## 📧 Contact & Support

- **Author**: Kevyn Murilo
- **GitHub**: [@KevynMurilo](https://github.com/KevynMurilo)
- **Issues**: [GitHub Issues](https://github.com/KevynMurilo/spring-cli/issues)
- **Discussions**: [GitHub Discussions](https://github.com/KevynMurilo/spring-cli/discussions)

---

<div align="center">

**⭐ Star this repository if you find it useful! ⭐**

Made with ❤️ by [Kevyn Murilo](https://github.com/KevynMurilo)

</div>
