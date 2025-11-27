package com.springcli.model.rules;

import java.util.List;

public record MavenConfig(
    List<MavenDependency> dependencies,
    List<MavenPlugin> plugins,
    List<MavenExclusion> exclusions
) {
}
