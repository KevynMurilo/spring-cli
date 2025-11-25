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
        boolean enableJwt = false;

        if (hasSecurity) {
            System.out.println("\n🔐 SECURITY FEATURES");
            System.out.println("  ℹ️  Spring Security is included. Configure JWT authentication:");
            enableJwt = uiSelector.askYesNo("    Enable JWT Authentication", presetFeatures.enableJwt());
        }

        System.out.println("\n📚 API DOCUMENTATION");
        System.out.println("  ℹ️  Add OpenAPI/Swagger documentation for your API:");
        boolean enableSwagger = uiSelector.askYesNo("    Enable Swagger/OpenAPI", presetFeatures.enableSwagger());

        System.out.println("\n🌐 CROSS-ORIGIN RESOURCE SHARING");
        System.out.println("  ℹ️  Configure CORS for frontend applications:");
        boolean enableCors = uiSelector.askYesNo("    Enable CORS Configuration", presetFeatures.enableCors());

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
                enableCors,
                enableExceptionHandler,
                presetFeatures.enableMapStruct(),
                enableDocker,
                enableKubernetes,
                enableCiCd,
                presetFeatures.enableAudit()
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
