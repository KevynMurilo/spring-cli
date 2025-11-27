package com.springcli.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcli.model.rules.DependencyRule;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RegisterReflectionForBinding(DependencyRule.class)
public class DependencyRulesService {

    private final Map<String, DependencyRule> rulesCache;
    private final ObjectMapper objectMapper;

    public DependencyRulesService() {
        this.objectMapper = new ObjectMapper();
        this.rulesCache = loadRules();
    }

    private Map<String, DependencyRule> loadRules() {
        try {
            ClassPathResource resource = new ClassPathResource("dependency-rules.json");
            List<DependencyRule> rules = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {}
            );

            return rules.stream()
                .collect(Collectors.toMap(DependencyRule::id, rule -> rule));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load dependency-rules.json", e);
        }
    }

    public Optional<DependencyRule> getRule(String dependencyId) {
        return Optional.ofNullable(rulesCache.get(dependencyId));
    }

    public List<DependencyRule> getRules(List<String> dependencyIds) {
        return dependencyIds.stream()
            .map(this::getRule)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(Comparator.comparingInt(DependencyRule::priority).reversed())
            .collect(Collectors.toList());
    }

    public List<DependencyRule> getAllRules() {
        return new ArrayList<>(rulesCache.values());
    }

    public boolean hasRule(String dependencyId) {
        return rulesCache.containsKey(dependencyId);
    }
}
