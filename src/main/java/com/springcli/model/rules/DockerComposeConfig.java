package com.springcli.model.rules;

import java.util.List;
import java.util.Map;

public record DockerComposeConfig(
    String serviceName,
    String image,
    List<String> ports,
    Map<String, String> environment,
    List<String> volumes,
    List<String> depends_on,
    HealthcheckConfig healthcheck
) {
}
