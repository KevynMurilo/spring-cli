package com.springcli.service.config;

import com.springcli.model.rules.DependencyRule;
import com.springcli.service.DependencyRulesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DependencyConfigurationRegistry {

    private final DependencyRulesService rulesService;

    public void initialize(String springBootVersion) {
        log.debug("Using dependency-rules.json for configuration");
    }

    public Optional<DependencyConfiguration> getConfiguration(String dependencyId) {
        return rulesService.getRule(dependencyId)
            .map(rule -> {
                Map<String, String> properties = rule.runtime().properties().stream()
                    .collect(Collectors.toMap(
                        p -> p.key(),
                        p -> p.value()
                    ));

                return DependencyConfiguration.builder(dependencyId)
                    .requiredProperties(properties)
                    .build();
            });
    }

    public List<DependencyConfiguration> getConfigurationsForDependencies(Set<String> dependencyIds) {
        return dependencyIds.stream()
                .map(this::getConfiguration)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public Map<String, String> aggregateProperties(Set<String> dependencyIds) {
        Map<String, String> allProperties = new HashMap<>();

        dependencyIds.forEach(depId -> {
            getConfiguration(depId).ifPresent(config ->
                allProperties.putAll(config.requiredProperties())
            );
        });

        return allProperties;
    }

    public List<DependencyRule> getRules(List<String> dependencyIds) {
        return rulesService.getRules(dependencyIds);
    }

    public Optional<DependencyRule> getRule(String dependencyId) {
        return rulesService.getRule(dependencyId);
    }
}
