package com.springcli.service;

import com.springcli.model.ProjectFeatures;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FeatureCustomizer {

    private final UISelector uiSelector;

    public ProjectFeatures customizeFeatures(ProjectFeatures presetFeatures, Set<String> dependencies) {
        boolean hasSecurity = dependencies.contains("security");
        boolean hasDataJpa = dependencies.contains("data-jpa");
        boolean hasWeb = dependencies.contains("web");
        boolean hasGraphQL = dependencies.contains("graphql");
        boolean hasActuator = dependencies.contains("actuator");
        boolean hasCloud = dependencies.stream().anyMatch(dep -> dep.startsWith("cloud-"));

        boolean enableJwt = false;
        boolean enableMapStruct = presetFeatures.enableMapStruct();
        boolean enableAudit = presetFeatures.enableAudit();
        boolean enableSwagger = presetFeatures.enableSwagger();

        // 🔐 SECURITY FEATURES
        if (hasSecurity) {
            System.out.println("\n🔐 SECURITY FEATURES");
            System.out.println("  ℹ️  Spring Security detected. Configure authentication:");
            enableJwt = uiSelector.askYesNo("    Enable JWT Authentication", presetFeatures.enableJwt());
        }

        // 📚 API DOCUMENTATION
        System.out.println("\n📚 API DOCUMENTATION");
        if (hasGraphQL) {
            System.out.println("  ℹ️  GraphQL detected! GraphQL Playground will be available at /graphiql");
            System.out.println("  ℹ️  Swagger is typically not needed with GraphQL (use GraphiQL instead):");
            enableSwagger = uiSelector.askYesNo("    Enable Swagger/OpenAPI anyway", false);
        } else {
            System.out.println("  ℹ️  Add OpenAPI/Swagger documentation for your API:");
            enableSwagger = uiSelector.askYesNo("    Enable Swagger/OpenAPI", presetFeatures.enableSwagger());
        }

        // 🌐 CORS CONFIGURATION
        if (hasWeb || hasGraphQL) {
            System.out.println("\n🌐 CROSS-ORIGIN RESOURCE SHARING");
            System.out.println("  ℹ️  Configure CORS for frontend applications:");
            boolean enableCors = uiSelector.askYesNo("    Enable CORS Configuration", presetFeatures.enableCors());

            // ⚠️ ERROR HANDLING
            System.out.println("\n⚠️  ERROR HANDLING");
            System.out.println("  ℹ️  Global exception handler for standardized error responses:");
            boolean enableExceptionHandler = uiSelector.askYesNo("    Enable Global Exception Handler",
                    presetFeatures.enableExceptionHandler());

            // 🗺️ ENTITY MAPPING
            if (hasDataJpa) {
                System.out.println("\n🗺️  ENTITY MAPPING");
                System.out.println("  ℹ️  JPA detected. MapStruct can help map entities to DTOs efficiently:");
                enableMapStruct = uiSelector.askYesNo("    Enable MapStruct for DTO mapping", presetFeatures.enableMapStruct());

                // 📝 DATABASE AUDIT
                System.out.println("\n📝 DATABASE AUDIT");
                System.out.println("  ℹ️  Add automatic audit fields (createdAt, updatedAt, createdBy, updatedBy):");
                enableAudit = uiSelector.askYesNo("    Enable JPA Auditing", presetFeatures.enableAudit());
            }

            // ☁️ CLOUD & MICROSERVICES
            if (hasCloud) {
                System.out.println("\n☁️  CLOUD & MICROSERVICES");
                System.out.println("  ℹ️  Spring Cloud dependencies detected!");
                if (hasActuator) {
                    System.out.println("  ✓ Actuator endpoints available for health checks and metrics");
                }
                System.out.println("  💡 Consider enabling Docker and Kubernetes for cloud deployment");
            }

            // 🐳 DEVOPS & INFRASTRUCTURE
            System.out.println("\n🐳 DEVOPS & INFRASTRUCTURE");
            System.out.println("  ℹ️  Container and deployment configurations:");
            boolean enableDocker = uiSelector.askYesNo("    Generate Docker files (Dockerfile + docker-compose)", presetFeatures.enableDocker());
            boolean enableKubernetes = uiSelector.askYesNo("    Generate Kubernetes manifests (deployment, service, configmap)",
                    presetFeatures.enableKubernetes());
            boolean enableCiCd = uiSelector.askYesNo("    Generate CI/CD pipeline (GitHub Actions)",
                    presetFeatures.enableCiCd());

            return new ProjectFeatures(
                    enableJwt,
                    enableSwagger,
                    enableCors,
                    enableExceptionHandler,
                    enableMapStruct,
                    enableDocker,
                    enableKubernetes,
                    enableCiCd,
                    enableAudit
            );
        }

        // Fallback para casos sem web/graphql
        System.out.println("\n⚠️  ERROR HANDLING");
        System.out.println("  ℹ️  Global exception handler for standardized error responses:");
        boolean enableExceptionHandler = uiSelector.askYesNo("    Enable Global Exception Handler",
                presetFeatures.enableExceptionHandler());

        System.out.println("\n🐳 DEVOPS & INFRASTRUCTURE");
        System.out.println("  ℹ️  Container and deployment configurations:");
        boolean enableDocker = uiSelector.askYesNo("    Generate Docker files", presetFeatures.enableDocker());
        boolean enableKubernetes = uiSelector.askYesNo("    Generate Kubernetes manifests",
                presetFeatures.enableKubernetes());
        boolean enableCiCd = uiSelector.askYesNo("    Generate CI/CD pipeline (GitHub Actions)",
                presetFeatures.enableCiCd());

        return new ProjectFeatures(
                enableJwt,
                enableSwagger,
                false, // enableCors
                enableExceptionHandler,
                enableMapStruct,
                enableDocker,
                enableKubernetes,
                enableCiCd,
                enableAudit
        );
    }

    public void printFeatureSummary(ProjectFeatures features, Set<String> dependencies) {
        System.out.println("\n╔══ FEATURE SUMMARY ═══════════════════════════════════════╗");

        if (dependencies.contains("security")) {
            printFeature("🔐 Security", features.enableJwt() ? "JWT Authentication" : "Basic Security");
        }

        printFeature("📚 API Docs", features.enableSwagger() ? "Swagger/OpenAPI Enabled" : "Disabled");
        printFeature("🌐 CORS", features.enableCors() ? "Enabled" : "Disabled");
        printFeature("⚠️  Error Handling", features.enableExceptionHandler() ? "Global Handler" : "Default");

        System.out.println("│                                                            │");
        System.out.println("│  DevOps:                                                   │");
        printFeature("  🐳 Docker", features.enableDocker() ? "Dockerfile + Compose" : "Not included");
        printFeature("  ☸️  Kubernetes", features.enableKubernetes() ? "K8s Manifests" : "Not included");
        printFeature("  🔄 CI/CD", features.enableCiCd() ? "GitHub Actions" : "Not included");

        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
    }

    private void printFeature(String name, String status) {
        System.out.printf("│  %-20s %-35s│%n", name + ":", status);
    }
}
