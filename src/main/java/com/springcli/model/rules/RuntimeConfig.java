package com.springcli.model.rules;

import java.util.List;

public record RuntimeConfig(
    List<PropertyConfig> properties
) {
}
