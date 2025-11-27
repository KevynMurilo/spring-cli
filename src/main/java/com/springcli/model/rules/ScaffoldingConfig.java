package com.springcli.model.rules;

import java.util.List;

public record ScaffoldingConfig(
    List<ScaffoldingFile> files
) {
}
