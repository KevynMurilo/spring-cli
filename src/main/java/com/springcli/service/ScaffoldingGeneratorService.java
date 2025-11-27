package com.springcli.service;

import com.springcli.model.rules.DependencyRule;
import com.springcli.model.rules.ScaffoldingFile;
import com.springcli.service.config.DependencyConfigurationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScaffoldingGeneratorService {

    private final DependencyConfigurationRegistry configRegistry;

    public Map<String, String> generateScaffoldingFiles(Set<String> dependencies, String basePackage, Path projectPath) {
        List<DependencyRule> rules = configRegistry.getRules(new ArrayList<>(dependencies));

        Map<String, String> filesToGenerate = new HashMap<>();

        for (DependencyRule rule : rules) {
            if (rule.scaffolding() != null && rule.scaffolding().files() != null) {
                for (ScaffoldingFile file : rule.scaffolding().files()) {
                    String resolvedPath = resolvePath(file.path(), basePackage, projectPath);
                    String resolvedContent = resolveContent(file.content(), basePackage);

                    filesToGenerate.put(resolvedPath, resolvedContent);
                    log.debug("Scaffolding file prepared: {}", resolvedPath);
                }
            }
        }

        log.info("Prepared {} scaffolding files for generation", filesToGenerate.size());
        return filesToGenerate;
    }

    private String resolvePath(String templatePath, String basePackage, Path projectPath) {
        String packagePath = basePackage.replace(".", "/");
        String resolved = templatePath.replace("{{basePackage}}", packagePath);
        return projectPath.resolve(resolved).toString();
    }

    private String resolveContent(String templateContent, String basePackage) {
        return templateContent.replace("{{basePackage}}", basePackage);
    }

    public List<DependencyRule> getRulesWithScaffolding(Set<String> dependencies) {
        List<DependencyRule> rules = configRegistry.getRules(new ArrayList<>(dependencies));

        return rules.stream()
            .filter(rule -> rule.scaffolding() != null)
            .filter(rule -> rule.scaffolding().files() != null)
            .filter(rule -> !rule.scaffolding().files().isEmpty())
            .toList();
    }
}
