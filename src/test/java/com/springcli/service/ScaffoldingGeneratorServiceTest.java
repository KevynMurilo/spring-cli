package com.springcli.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScaffoldingGeneratorServiceTest {

    @Autowired
    private ScaffoldingGeneratorService service;

    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateSecurityConfigForSecurityDependency() {
        Set<String> dependencies = Set.of("security");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        assertThat(files).containsKey(tempDir.resolve("src/main/java/com/example/app/config/SecurityConfig.java").toString());

        String content = files.get(tempDir.resolve("src/main/java/com/example/app/config/SecurityConfig.java").toString());
        assertThat(content).contains("package com.example.app.config;");
        assertThat(content).contains("@EnableWebSecurity");
    }

    @Test
    void shouldGenerateJwtServiceForJwtDependency() {
        Set<String> dependencies = Set.of("jwt");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        assertThat(files).containsKey(tempDir.resolve("src/main/java/com/example/app/security/JwtService.java").toString());

        String content = files.get(tempDir.resolve("src/main/java/com/example/app/security/JwtService.java").toString());
        assertThat(content).contains("package com.example.app.security;");
        assertThat(content).contains("public class JwtService");
    }

    @Test
    void shouldGenerateSwaggerConfigForSwaggerDependency() {
        Set<String> dependencies = Set.of("swagger");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        String configPath = tempDir.resolve("src/main/java/com/example/app/config/SwaggerConfig.java").toString();
        assertThat(files).containsKey(configPath);

        String content = files.get(configPath);
        assertThat(content).contains("package com.example.app.config;");
        assertThat(content).contains("public OpenAPI customOpenAPI()");
    }

    @Test
    void shouldGenerateMultipleFilesForMultipleDependencies() {
        Set<String> dependencies = Set.of("security", "jwt", "swagger");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void shouldReturnEmptyForDependenciesWithoutScaffolding() {
        Set<String> dependencies = Set.of("lombok", "actuator");
        String basePackage = "com.example.app";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isEmpty();
    }

    @Test
    void shouldReplaceBasePackagePlaceholder() {
        Set<String> dependencies = Set.of("security");
        String basePackage = "org.test.myapp";

        Map<String, String> files = service.generateScaffoldingFiles(dependencies, basePackage, tempDir);

        assertThat(files).isNotEmpty();
        files.values().forEach(content -> {
            assertThat(content).doesNotContain("{{basePackage}}");
            if (content.contains("package")) {
                assertThat(content).contains("org.test.myapp");
            }
        });
    }
}
