package com.springcli.service;

import com.springcli.model.rules.DependencyRule;
import com.springcli.model.rules.DockerComposeConfig;
import com.springcli.service.config.DependencyConfigurationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerComposeGeneratorService {

    private final DependencyConfigurationRegistry configRegistry;

    public String generateDockerCompose(Set<String> dependencies) {
        List<DependencyRule> rules = configRegistry.getRules(new ArrayList<>(dependencies));

        List<DockerComposeConfig> services = rules.stream()
            .filter(rule -> rule.infrastructure() != null)
            .filter(rule -> rule.infrastructure().dockerCompose() != null)
            .map(rule -> rule.infrastructure().dockerCompose())
            .collect(Collectors.toList());

        if (services.isEmpty()) {
            return null;
        }

        StringBuilder yaml = new StringBuilder();
        yaml.append("version: '3.8'\n\n");
        yaml.append("services:\n");

        for (DockerComposeConfig service : services) {
            yaml.append(generateServiceYaml(service));
        }

        yaml.append("\nvolumes:\n");
        Set<String> volumes = extractVolumes(services);
        for (String volume : volumes) {
            yaml.append("  ").append(volume).append(":\n");
        }

        return yaml.toString();
    }

    private String generateServiceYaml(DockerComposeConfig service) {
        StringBuilder yaml = new StringBuilder();

        yaml.append("  ").append(service.serviceName()).append(":\n");
        yaml.append("    image: ").append(service.image()).append("\n");

        if (service.ports() != null && !service.ports().isEmpty()) {
            yaml.append("    ports:\n");
            for (String port : service.ports()) {
                yaml.append("      - \"").append(port).append("\"\n");
            }
        }

        if (service.environment() != null && !service.environment().isEmpty()) {
            yaml.append("    environment:\n");
            service.environment().forEach((key, value) ->
                yaml.append("      ").append(key).append(": ").append(value).append("\n")
            );
        }

        if (service.volumes() != null && !service.volumes().isEmpty()) {
            yaml.append("    volumes:\n");
            for (String volume : service.volumes()) {
                yaml.append("      - ").append(volume).append("\n");
            }
        }

        if (service.depends_on() != null && !service.depends_on().isEmpty()) {
            yaml.append("    depends_on:\n");
            for (String dep : service.depends_on()) {
                yaml.append("      - ").append(dep).append("\n");
            }
        }

        if (service.healthcheck() != null) {
            yaml.append("    healthcheck:\n");
            yaml.append("      test: ").append(service.healthcheck().test()).append("\n");
            yaml.append("      interval: ").append(service.healthcheck().interval()).append("\n");
            yaml.append("      timeout: ").append(service.healthcheck().timeout()).append("\n");
            yaml.append("      retries: ").append(service.healthcheck().retries()).append("\n");
        }

        yaml.append("\n");
        return yaml.toString();
    }

    private Set<String> extractVolumes(List<DockerComposeConfig> services) {
        Set<String> volumes = new HashSet<>();

        for (DockerComposeConfig service : services) {
            if (service.volumes() != null) {
                for (String volume : service.volumes()) {
                    String volumeName = volume.split(":")[0];
                    if (!volumeName.startsWith(".") && !volumeName.startsWith("/")) {
                        volumes.add(volumeName);
                    }
                }
            }
        }

        return volumes;
    }
}
