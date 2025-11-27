package com.springcli.model.rules;

import java.util.List;

public record HealthcheckConfig(
    List<String> test,
    String interval,
    String timeout,
    int retries
) {
}
