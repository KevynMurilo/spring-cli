# Spring CLI - Modern Spring Boot Project Generator

A powerful, native CLI tool built with **Spring Shell** and **GraalVM Native Image** for scaffolding Spring Boot projects with custom architectures and best practices.

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

## 🌟 Features

- **10 Architecture Patterns**: MVC, Clean, Hexagonal, DDD, CQRS, Event-Driven, and more
- **5 Built-in Presets**: REST-API, GraphQL-API, Microservice, Monolith, Minimal
- **🎨 Custom Preset Management**: Create, edit, and delete your own project templates
- **🧠 Intelligent Auto-Configuration**: Smart feature suggestions based on selected dependencies
- **Interactive Wizard**: Beautiful TUI for project configuration with auto-start menu
- **Template Engine**: Pebble Templates for dynamic code generation
- **Smart Dependency Management**: Automatic injection of JWT, Swagger, MapStruct, and more
- **DevOps Ready**: Automatic generation of Dockerfile, docker-compose, CI/CD pipelines, and Kubernetes manifests
- **Fast Native Compilation**: GraalVM Native Image for instant startup
- **Clean Architecture**: Following SOLID principles and best practices

---

## 📥 Installation

### Option 1: Download Pre-built Release (Recommended)

1. **Download the latest release:**
   - Go to [Releases](https://github.com/KevynMurilo/spring-cli/releases)
   - Download the appropriate file for your system:
     - `spring-cli-1.0.0.jar` - For any OS with Java 17+ installed
     - `spring-cli.exe` - Native Windows executable (if available)
     - `spring-cli` - Native Linux/Mac executable (if available)

2. **Run the application:**

   **Using JAR (requires Java 17+):**
   ```bash
   java -jar spring-cli-1.0.0.jar
   ```

   **Using Native Executable (Windows):**
   ```bash
   spring-cli.exe
   ```

   **Using Native Executable (Linux/Mac):**
   ```bash
   ./spring-cli
   ```

3. **Optional: Add to PATH for global access**

   **Windows:**
   ```bash
   # Move to a permanent location
   move spring-cli.exe C:\tools\spring-cli.exe

   # Add to PATH environment variable
   # Then you can run from anywhere: spring-cli
   ```

   **Linux/Mac:**
   ```bash
   # Move to /usr/local/bin
   sudo cp spring-cli /usr/local/bin/
   sudo chmod +x /usr/local/bin/spring-cli

   # Now you can run from anywhere: spring-cli
   ```

---

### Option 2: Build from Source

#### Prerequisites
- **Java 21+** (for development)
- **Maven 3.8+**
- **GraalVM 21+** (optional, for native image compilation)

#### Build JAR
```bash
# Clone the repository
git clone https://github.com/KevynMurilo/spring-cli.git
cd spring-cli

# Build with Maven
mvn clean package -DskipTests

# Run the JAR
java -jar target/spring-cli-1.0.0.jar
```

#### Build Native Image (Optional)
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

---

## 🚀 Quick Start

### First Time User?

When you start the CLI, you'll see comprehensive instructions:

```bash
java -jar spring-cli-1.0.0.jar
```

You'll be greeted with:
```
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║  Welcome to Spring CLI - Modern Spring Boot Generator        ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  📖 HOW TO USE:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  1️⃣  Interactive Menu (Recommended)
     Type: m or menu
     → Navigate with arrow keys, select with ENTER

  2️⃣  Quick Generation
     Type: new <project-name> [options]
     Example: new my-api --groupId=com.company

  3️⃣  Full Generation
     Type: generate
     → Choose presets and configure everything

  💡 First time? Just type: m and press ENTER!
```

---

## 📖 Usage

### 1. Interactive Menu (Easiest Way)

Start the interactive menu:
```bash
spring:> m
```

Or:
```bash
spring:> menu
```

This shows a beautiful interactive menu with options:
1. 🚀 **Generate New Project** - Create a complete Spring Boot project
2. 📦 **Quick Generate** - Fast project generation (interactive)
3. ⭐ **Manage Presets** - Create, edit, or delete custom presets
4. 📋 **List Presets** - View available project templates
5. ⚙️ **Configure CLI** - Set default preferences (interactive)
6. 🛠️ **Utilities** - Clear cache, refresh metadata, system info
7. ℹ️ **About** - Information about Spring CLI
8. ❌ **Exit** - Close the application

---

### 2. Quick Generation (Interactive)

Fast project generation with interactive prompts:
```bash
spring:> m
# Select: Quick Generate
# Then follow the prompts:
# - Project name: my-awesome-api
# - Group ID: com.mycompany
# - Architecture: CLEAN
# - Output directory: ./projects
```

---

### 3. Command-Line Quick Generation

Generate a project with a single command:
```bash
spring:> new my-api
```

With options:
```bash
spring:> new my-api --groupId=com.example --architecture=HEXAGONAL --output=./projects
```

**Available options:**
- `--groupId=<value>` - Maven group ID (default: com.example)
- `--architecture=<value>` - Architecture pattern (default: CLEAN)
- `--output=<path>` - Output directory (default: current directory)

---

### 4. Full Generation with Presets

Generate with full control and presets:
```bash
spring:> generate
```

Follow the wizard:
1. Select a preset or start from scratch
2. Configure project details (name, group, package)
3. Choose Spring Boot version
4. Select build tool (Maven/Gradle)
5. Choose Java version
6. Select packaging (JAR/WAR)
7. Pick architecture
8. Manage dependencies interactively
9. Customize features (JWT, Swagger, Docker, etc.)

---

## 🎨 Custom Preset Management

Create and manage your own project templates!

### Access Preset Manager
```bash
spring:> preset-manager
```

Or via menu:
```bash
spring:> m
# Select: Manage Presets
```

### What You Can Do

**➕ Create New Preset**
- Build a custom template from scratch
- Choose architecture, Java version, dependencies, and features
- Save for reuse across projects

**✏️ Edit Existing Preset**
- Modify any preset (including built-in ones)
- Editing built-in presets creates a custom copy
- Full control over all configuration options

**📋 List All Presets**
- View built-in and custom presets
- See architecture, Java version, and dependency count

**🗑️ Delete Preset**
- Remove custom presets
- Built-in presets cannot be deleted

### Example Use Case

**Team Standardization:**
1. Create a custom preset "Team Standard API"
2. Configure: Clean Architecture + Java 21 + PostgreSQL + JWT + Docker
3. Share the preset file with your team (`~/.spring-cli/presets/team-standard-api.json`)
4. Everyone generates consistent projects with one click

👉 **Full Documentation**: [Preset Management Guide](docs/PRESET_MANAGEMENT.md)

---

## 🛠️ Available Commands

### Interactive Commands
- `m` or `menu` - Open interactive menu (recommended)
- `generate` - Start project generation wizard
- `preset-manager` - Manage custom presets

### Quick Commands
- `new <name> [options]` - Quick project generation
- `list-presets` - List all available presets
- `show-config` - Show current configuration
- `reset-config` - Reset configuration to defaults

### Utility Commands
- `clear-cache` - Clear metadata cache
- `refresh-metadata` - Refresh metadata from Spring Initializr
- `info` - Show system information
- `version` - Show CLI version
- `clear` - Clear terminal screen
- `help` - Show all available commands
- `exit` - Exit the application

---

## 🏗️ Supported Architectures

1. **MVC** - Model-View-Controller
2. **LAYERED** - Layered Architecture
3. **CLEAN** - Clean Architecture
4. **HEXAGONAL** - Ports & Adapters
5. **FEATURE_DRIVEN** - Feature-Driven
6. **DDD** - Domain-Driven Design
7. **CQRS** - Command Query Responsibility Segregation
8. **EVENT_DRIVEN** - Event-Driven
9. **ONION** - Onion Architecture
10. **VERTICAL_SLICE** - Vertical Slice

Each architecture comes with a proper project structure and code organization.

---

## 📦 Built-in Presets

### REST-API
- **Architecture:** Clean
- **Dependencies:** web, data-jpa, h2, validation, lombok, devtools
- **Features:** JWT, Swagger, CORS, Exception Handler, MapStruct, Audit

### GraphQL-API ⭐ NEW
- **Architecture:** Clean
- **Dependencies:** web, graphql, data-jpa, h2, validation, lombok, devtools
- **Features:** JWT, CORS, Exception Handler, MapStruct, Audit
- **Bonus:** GraphQL Playground at `/graphiql`

### Microservice
- **Architecture:** Hexagonal
- **Dependencies:** web, data-jpa, postgresql, eureka, config-client, actuator, lombok
- **Features:** All enabled (JWT, Swagger, CORS, Exception Handler, MapStruct, Docker, Kubernetes, CI/CD, Audit)

### Monolith
- **Architecture:** MVC
- **Dependencies:** web, thymeleaf, data-jpa, mysql, security, validation, lombok
- **Features:** Exception Handler, Docker

### Minimal
- **Architecture:** MVC
- **Dependencies:** web, lombok, devtools
- **Features:** None (minimal setup for quick prototyping)

---

## ⚙️ Configuration

Configure default values for project generation:

```bash
spring:> m
# Select: Configure CLI
```

Or view current config:
```bash
spring:> show-config
```

**Configurable options:**
- Default Group ID
- Default Java Version
- Default Packaging (JAR/WAR)
- Default Architecture
- Default Output Directory
- Auto-open IDE after generation
- Preferred IDE (IntelliJ IDEA, VS Code, Eclipse)
- Generate README.md by default
- Generate .gitignore by default
- Use application.yml instead of properties

Configuration is stored in: `~/.springclirc.json`

---

## 🎯 Features

### Automatic Feature Generation

Based on your dependencies, the CLI can automatically generate:

- **JWT Authentication** - When Spring Security is included
- **Swagger/OpenAPI Documentation** - API documentation at `/swagger-ui.html`
- **CORS Configuration** - Cross-origin resource sharing setup
- **Global Exception Handler** - Standardized error responses
- **MapStruct Integration** - DTO mapping for JPA entities
- **JPA Auditing** - Automatic createdAt, updatedAt, createdBy, updatedBy fields
- **Docker Files** - Dockerfile + docker-compose.yml
- **Kubernetes Manifests** - Deployment, Service, ConfigMap
- **CI/CD Pipeline** - GitHub Actions workflow

### Intelligent Suggestions

The CLI intelligently suggests features based on your selections:

- **GraphQL detected** → Suggests disabling Swagger (use GraphiQL instead)
- **JPA detected** → Suggests MapStruct and Auditing
- **Spring Cloud detected** → Shows tips about deployment
- **Actuator included** → Informs about health checks and metrics

---

## 🌐 Project Structure

Generated projects follow best practices with a clean structure:

```
my-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/myproject/
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       ├── entity/          # JPA entities
│   │   │       ├── exception/       # Custom exceptions
│   │   │       ├── mapper/          # MapStruct mappers
│   │   │       ├── repository/      # JPA repositories
│   │   │       ├── service/         # Business logic
│   │   │       └── Application.java # Main class
│   │   └── resources/
│   │       ├── application.yml      # Main config
│   │       ├── application-dev.yml  # Dev config
│   │       ├── application-prod.yml # Prod config
│   │       └── application-test.yml # Test config
│   └── test/
├── Dockerfile                       # Docker image
├── docker-compose.yml              # Docker Compose
├── k8s/                            # Kubernetes manifests
├── .github/workflows/              # CI/CD pipeline
├── .gitignore
├── README.md
└── pom.xml                         # Maven configuration
```

*Structure varies based on selected architecture*

---

## 📚 Documentation

- **[Preset Management Guide](docs/PRESET_MANAGEMENT.md)** - Complete guide to custom presets
- **[Architecture Guide](docs/ARCHITECTURES.md)** - Details on supported architectures
- **[Build Guide](docs/BUILD.md)** - Building and running Spring CLI
- **[Contributing Guide](docs/CONTRIBUTING.md)** - Contributing to Spring CLI

---

## 💡 Examples

### Example 1: Quick REST API
```bash
spring:> new my-api --groupId=com.company --architecture=CLEAN
```

### Example 2: Microservice with Preset
```bash
spring:> generate
# Select: Microservice preset
# Follow prompts
```

### Example 3: Custom GraphQL API
```bash
spring:> m
# Select: Quick Generate
# Project name: graphql-api
# Group ID: com.mycompany
# Architecture: CLEAN
# Output: ./projects
```

---

## 🔧 Troubleshooting

### JAR doesn't run
**Issue:** `java -jar spring-cli-1.0.0.jar` fails

**Solution:**
1. Check Java version: `java -version` (must be 17+)
2. Download the latest Java: https://adoptium.net/

### Metadata not loading
**Issue:** Dependencies not showing

**Solution:**
```bash
spring:> m
# Select: Utilities
# Select: Refresh Metadata
```

Or:
```bash
spring:> refresh-metadata
```

### Clear cache
**Issue:** Cached data causing issues

**Solution:**
```bash
spring:> m
# Select: Utilities
# Select: Clear Cache
```

Or:
```bash
spring:> clear-cache
```

---

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](docs/CONTRIBUTING.md) for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- Spring Initializr for the metadata API
- Pebble template engine for code generation

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/KevynMurilo/spring-cli/issues)
- **Discussions:** [GitHub Discussions](https://github.com/KevynMurilo/spring-cli/discussions)

---

## 🔮 Roadmap

- [ ] Support for Gradle Kotlin DSL
- [ ] More built-in presets (Event Sourcing, CQRS+ES)
- [ ] Preset templates repository (community presets)
- [ ] IDE plugins (IntelliJ, VS Code)
- [ ] Web UI for project generation

---

**Made with ❤️ by [Kevyn Murilo](https://github.com/KevynMurilo)**

**⭐ Star this repo if you find it helpful!**
