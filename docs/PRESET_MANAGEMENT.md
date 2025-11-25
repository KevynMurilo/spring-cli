# 🎨 Preset Management Guide

The Spring CLI now includes a comprehensive preset management system that allows you to create, edit, and delete custom project templates. This feature enables you to save your favorite configurations and reuse them across multiple projects.

## Table of Contents

- [What are Presets?](#what-are-presets)
- [Accessing Preset Manager](#accessing-preset-manager)
- [Creating a Custom Preset](#creating-a-custom-preset)
- [Editing Presets](#editing-presets)
- [Deleting Presets](#deleting-presets)
- [Built-in vs Custom Presets](#built-in-vs-custom-presets)
- [Preset Storage](#preset-storage)
- [Examples](#examples)

---

## What are Presets?

Presets are pre-configured project templates that include:

- **Architecture** - The architectural pattern (MVC, Clean, Hexagonal, DDD, etc.)
- **Java Version** - Target Java version (17, 21, etc.)
- **Dependencies** - Spring Boot dependencies (Web, JPA, Security, etc.)
- **Features** - Additional features (JWT, Swagger, Docker, Kubernetes, etc.)

## Accessing Preset Manager

### From Main Menu
1. Start the Spring CLI
2. The interactive menu appears automatically
3. Select **"⭐ Manage Presets"**

### From Command Line
```bash
preset-manager
```

## Creating a Custom Preset

### Step-by-Step Process

1. **Select "Create New Preset"** from the Preset Manager menu

2. **Enter Preset Details**
   ```
   Preset Name: My GraphQL API
   Description: Production-ready GraphQL API with authentication
   ```

3. **Choose Architecture**
   - Select from 10 available architectures
   - Example: Clean Architecture

4. **Select Java Version**
   - Choose target Java version
   - Example: Java 21

5. **Select Dependencies**
   - Interactive dependency selector by category
   - Example: web, graphql, data-jpa, postgresql, security, validation

6. **Configure Features**
   The system intelligently suggests features based on your dependencies:

   **Security Features** (if security is selected)
   - JWT Authentication

   **API Documentation**
   - Swagger/OpenAPI (automatically suggests no if GraphQL detected)
   - GraphQL Playground (automatic with GraphQL)

   **Cross-Origin Resource Sharing**
   - CORS Configuration

   **Error Handling**
   - Global Exception Handler

   **Entity Mapping** (if JPA is selected)
   - MapStruct for DTO mapping
   - JPA Auditing (createdAt, updatedAt fields)

   **Cloud & Microservices** (if Spring Cloud detected)
   - Informational messages about actuator endpoints

   **DevOps & Infrastructure**
   - Docker files (Dockerfile + docker-compose)
   - Kubernetes manifests (deployment, service, configmap)
   - CI/CD pipeline (GitHub Actions)

7. **Confirmation**
   - Review your preset summary
   - Preset is saved to `~/.spring-cli/presets/`

## Editing Presets

### Editing Custom Presets

1. Select **"✏️ Edit Existing Preset"**
2. Choose the preset to edit
3. Modify any field (press ENTER to keep current value)
4. Save changes

### Editing Built-in Presets

Built-in presets (REST-API, GraphQL-API, Microservice, Monolith, Minimal) can be edited:

- When you edit a built-in preset, a **custom copy** is created
- The original built-in preset remains unchanged
- Your custom version will be saved with the same name
- Custom presets take precedence over built-in ones with the same name

**Example:**
```
1. Edit built-in "REST-API" preset
2. Change architecture from Clean to Hexagonal
3. Save
4. A custom "REST-API" preset is created in ~/.spring-cli/presets/
5. Your custom version will be used when selecting "REST-API"
```

## Deleting Presets

### Delete Custom Presets

1. Select **"🗑️ Delete Preset"**
2. Choose the custom preset to delete
3. Review preset details
4. Confirm deletion

**Note:** Only custom presets can be deleted. Built-in presets cannot be deleted but can be overridden by creating custom presets with the same name.

## Built-in vs Custom Presets

### Built-in Presets

**Location:** Hardcoded in the application
**Modifiable:** Can be overridden by custom presets
**Deletable:** No

**Available Built-in Presets:**

1. **REST-API**
   - Architecture: Clean
   - Dependencies: web, data-jpa, h2, validation, lombok, devtools
   - Features: JWT, Swagger, CORS, Exception Handler, MapStruct, Audit

2. **GraphQL-API** ⭐ NEW
   - Architecture: Clean
   - Dependencies: web, graphql, data-jpa, h2, validation, lombok, devtools
   - Features: JWT, CORS, Exception Handler, MapStruct, Audit

3. **Microservice**
   - Architecture: Hexagonal
   - Dependencies: web, data-jpa, postgresql, eureka, config-client, actuator, lombok
   - Features: All enabled (JWT, Swagger, CORS, Exception Handler, MapStruct, Docker, Kubernetes, CI/CD, Audit)

4. **Monolith**
   - Architecture: MVC
   - Dependencies: web, thymeleaf, data-jpa, mysql, security, validation, lombok
   - Features: Exception Handler, Docker

5. **Minimal**
   - Architecture: MVC
   - Dependencies: web, lombok, devtools
   - Features: None (minimal setup)

### Custom Presets

**Location:** `~/.spring-cli/presets/` (JSON files)
**Modifiable:** Yes
**Deletable:** Yes
**Sharable:** Yes (copy JSON files)

## Preset Storage

### Location
```
~/.spring-cli/presets/
```

On Windows:
```
C:\Users\<username>\.spring-cli\presets\
```

On Linux/Mac:
```
/home/<username>/.spring-cli/presets/
```

### File Format

Custom presets are stored as JSON files:

```json
{
  "name": "My GraphQL API",
  "description": "Production-ready GraphQL API with authentication",
  "architecture": "CLEAN",
  "javaVersion": "21",
  "dependencies": [
    "web",
    "graphql",
    "data-jpa",
    "postgresql",
    "security",
    "validation",
    "lombok"
  ],
  "features": {
    "enableJwt": true,
    "enableSwagger": false,
    "enableCors": true,
    "enableExceptionHandler": true,
    "enableMapStruct": true,
    "enableDocker": true,
    "enableKubernetes": false,
    "enableCiCd": true,
    "enableAudit": true
  },
  "builtIn": false
}
```

### Sharing Presets

You can share custom presets by copying the JSON files:

1. Copy preset file from `~/.spring-cli/presets/`
2. Share with team members
3. Team members place file in their `~/.spring-cli/presets/` directory
4. Preset becomes available immediately

## Examples

### Example 1: Team Standard API Template

**Scenario:** Your team always uses the same stack for REST APIs

**Solution:** Create a custom preset

```
Name: Team Standard API
Description: Our standard REST API configuration
Architecture: Clean Architecture
Java Version: 21
Dependencies:
  - web
  - data-jpa
  - postgresql
  - security
  - validation
  - actuator
  - lombok
  - devtools
Features:
  - JWT Authentication: Yes
  - Swagger: Yes
  - CORS: Yes
  - Exception Handler: Yes
  - MapStruct: Yes
  - Docker: Yes
  - Kubernetes: Yes
  - CI/CD: Yes
  - Audit: Yes
```

**Benefit:** Team members can generate consistent projects with one click

---

### Example 2: Microservice with Event Sourcing

**Scenario:** You frequently build microservices with event-driven architecture

**Solution:** Create specialized preset

```
Name: Event-Driven Microservice
Description: Microservice with Kafka and event sourcing
Architecture: Event-Driven
Java Version: 21
Dependencies:
  - web
  - kafka
  - data-jpa
  - postgresql
  - cloud-eureka
  - cloud-config-client
  - actuator
  - lombok
Features:
  - All DevOps features enabled
  - Kubernetes manifests
  - CI/CD pipeline
```

---

### Example 3: Quick Prototype Template

**Scenario:** You need a minimal setup for quick prototypes

**Solution:** Edit the Minimal preset or create new one

```
Name: Quick Prototype
Description: Fast setup for testing ideas
Architecture: MVC
Java Version: 21
Dependencies:
  - web
  - data-jpa
  - h2
  - lombok
  - devtools
Features:
  - All disabled (fastest startup)
```

---

## Tips & Best Practices

### 🎯 Naming Convention

Use descriptive names that indicate purpose:
- ✅ "Production REST API"
- ✅ "Microservice with Kafka"
- ✅ "GraphQL Mobile Backend"
- ❌ "My Project"
- ❌ "Test"

### 📝 Descriptions

Write clear descriptions that explain when to use the preset:
- ✅ "REST API for production with full monitoring and security"
- ✅ "Event-driven microservice for order processing"
- ❌ "API"
- ❌ "Preset 1"

### 🔄 Regular Updates

- Review your presets quarterly
- Update to latest Java versions
- Add newly available dependencies
- Remove deprecated features

### 👥 Team Sharing

Create a team repository for shared presets:
```
team-presets/
├── backend-api.json
├── microservice-template.json
├── graphql-gateway.json
└── README.md
```

### 🧪 Testing

After creating a preset:
1. Generate a test project using it
2. Verify all dependencies are correct
3. Check that features work as expected
4. Adjust and save changes

---

## Troubleshooting

### Preset Not Appearing

**Issue:** Custom preset not showing in list

**Solutions:**
1. Check file exists in `~/.spring-cli/presets/`
2. Verify JSON format is valid
3. Restart Spring CLI
4. Check file permissions

### Cannot Edit Built-in Preset

**Issue:** Error when trying to edit built-in preset

**Solution:** This is expected behavior. Edit creates a custom copy. The original built-in remains unchanged.

### Preset Name Conflicts

**Issue:** Custom preset has same name as built-in

**Solution:** Custom presets take precedence. To use the original built-in, delete or rename the custom preset.

---

## Related Documentation

- [Architecture Guide](ARCHITECTURES.md) - Details on supported architectures
- [Build Guide](BUILD.md) - Building and running Spring CLI
- [Contributing](CONTRIBUTING.md) - Contributing to Spring CLI

---

## Feedback & Support

Found a bug or have a feature request?
- GitHub Issues: https://github.com/KevynMurilo/spring-cli/issues
- Documentation: https://github.com/KevynMurilo/spring-cli/docs
